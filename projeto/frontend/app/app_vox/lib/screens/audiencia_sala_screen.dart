import 'dart:async';
import 'package:flutter/material.dart';
import 'package:livekit_client/livekit_client.dart' as livekit;
import 'package:permission_handler/permission_handler.dart';
import '../models/sala.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
import '../services/sala_service.dart';
import '../theme/vox_app_bar.dart';

/// URL do servidor LiveKit — mesmo host do backend, porta 7880.
final _liveKitUrl = 'ws://${Uri.parse(ApiClient.baseUrl).host}:7880';

class AudienciaSalaScreen extends StatefulWidget {
  final int salaId;
  const AudienciaSalaScreen({super.key, required this.salaId});

  @override
  State<AudienciaSalaScreen> createState() => _AudienciaSalaScreenState();
}

class _AudienciaSalaScreenState extends State<AudienciaSalaScreen> {
  final _authService = AuthService();
  final _salaService = SalaService();
  final _projectService = ProjectService();

  Sala? _sala;
  bool _isLoading = true;
  String? _loadError;
  bool _isModerator = false;

  livekit.Room? _room;
  livekit.EventsListener<livekit.RoomEvent>? _listener;
  String _connectionState = 'idle';

  Timer? _pollTimer;
  Timer? _pendingPollTimer;

  List<SolicitacaoEntrada> _pendingRequests = [];
  final Map<int, String> _requestNames = {};
  final Map<int, ({bool audio, bool video})> _mediaPermissions = {};

  /// userIds que pediram para falar (via backend /solicitacoes-fala)
  List<int> _speakRequestIds = [];

  /// Flags locais de mídia
  bool _micOn = false;
  bool _camOn = false;

  /// Permissões concedidas pelo moderador (para o cidadão)
  bool _canPublishAudio = false;
  bool _canPublishVideo = false;

  /// Cidadão já solicitou permissão para falar/câmera
  bool _requestingPermission = false;
  int? _revokingUserId;
  String _speechRequestId = '';
  bool _syncingPermissions = false;

  // ── Helpers de identidade ──────────────────────────────────

  bool _isMod(livekit.Participant<livekit.TrackPublication> p) {
    final sala = _sala;
    if (sala == null) return false;
    return int.tryParse(p.identity) == sala.moderatorId;
  }

  livekit.Participant<livekit.TrackPublication>? get _moderatorParticipant {
    final room = _room;
    if (room == null) return null;
    final local = room.localParticipant;
    final List<livekit.Participant<livekit.TrackPublication>> all = [
      ...room.remoteParticipants.values,
    ];
    if (local != null) all.insert(0, local);
    try {
      return all.firstWhere((p) => _isMod(p));
    } catch (_) {
      return null;
    }
  }

  List<livekit.Participant<livekit.TrackPublication>> get _citizenParticipants {
    final room = _room;
    if (room == null) return [];
    final local = room.localParticipant;
    final List<livekit.Participant<livekit.TrackPublication>> all = [
      ...room.remoteParticipants.values,
    ];
    if (local != null) all.insert(0, local);
    return all.where((p) => !_isMod(p)).toList();
  }

  // ── Ciclo de vida ──────────────────────────────────────────

  @override
  void initState() {
    super.initState();
    _init();
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    _pendingPollTimer?.cancel();
    _listener?.dispose();
    _room?.disconnect();
    super.dispose();
  }

  Future<void> _init() async {
    final role = await _authService.getUserRole();
    _isModerator = role == 'MODERATOR' || role == 'ADMINISTRATOR';
    await _loadSala();
  }

  Future<void> _loadSala() async {
    setState(() {
      _isLoading = true;
      _loadError = null;
    });
    _mediaPermissions.clear(); // Initialize the map
    try {
      final sala = await _salaService.getSalaById(widget.salaId);
      if (!mounted) return;
      setState(() {
        _sala = sala;
        _isLoading = false;
      });
      if (_isModerator) {
        _connectToRoom();
        _loadPendingRequests();
        _loadSpeakRequests();
        // Moderador: recarrega solicitações a cada 5 s para ver novos pedidos
        _pendingPollTimer = Timer.periodic(const Duration(seconds: 5), (_) {
          _loadPendingRequests();
          _loadSpeakRequests();
        });
      } else {
        _requestEntry();
        _pendingPollTimer = Timer.periodic(const Duration(seconds: 1), (_) {
          _syncCitizenPermissions();
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _loadError = 'Sala não encontrada.';
          _isLoading = false;
        });
      }
    }
  }

  // ── Fluxo cidadão ──────────────────────────────────────────

  Future<void> _requestEntry() async {
    setState(() => _connectionState = 'requesting');

    // 1) SEMPRE registra o pedido de entrada primeiro, para o moderador ver
    //    a solicitação (mesmo que o backend gere token para não aprovados).
    try {
      await _salaService.solicitarEntrada(widget.salaId);
    } catch (_) {
      // ignora — pode já existir uma solicitação anterior
    }

    // 2) Tenta obter token (se já estiver aprovado, conecta direto).
    try {
      final token = await _salaService.gerarToken(widget.salaId);
      await _connectToRoom(token: token);
      return;
    } catch (_) {
      // Ainda não aprovado — aguarda e vai tentando.
    }

    if (mounted) setState(() => _connectionState = 'waiting');
    _startPolling();
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(const Duration(seconds: 5), (_) => _tryJoin());
  }

  Future<void> _tryJoin() async {
    if (!mounted) {
      _pollTimer?.cancel();
      return;
    }
    if (_connectionState == 'connected' || _connectionState == 'connecting') {
      return;
    }
    try {
      final token = await _salaService.gerarToken(widget.salaId);
      _pollTimer?.cancel();
      _pollTimer = null;
      await _connectToRoom(token: token);
    } catch (_) {}
  }

  /// Reinicia o fluxo de entrada após uma falha (botão "Tentar novamente").
  Future<void> _retryEntry() async {
    _pollTimer?.cancel();
    await _room?.disconnect();
    _room = null;
    if (!mounted) return;
    if (_isModerator) {
      _connectToRoom();
    } else {
      _requestEntry();
    }
  }

  // ── Conexão LiveKit ────────────────────────────────────────

  Future<void> _requestMediaPermissions() async {
    final results = await [Permission.microphone, Permission.camera].request();
    final micOk = results[Permission.microphone]?.isGranted ?? false;
    final camOk = results[Permission.camera]?.isGranted ?? false;
    _micOn = micOk;
    _camOn = camOk;
  }

  Future<void> _connectToRoom({String? token}) async {
    setState(() => _connectionState = 'connecting');
    try {
      // Moderador publica de imediato → precisa de permissões antes de conectar.
      // Cidadão entra só para assistir; pede permissão só quando for publicar.
      if (_isModerator) {
        await _requestMediaPermissions();
      }

      final authToken =
          token ??
          await _salaService
              .gerarToken(widget.salaId)
              .timeout(const Duration(seconds: 10));
      // Mesmas opções do site (adaptiveStream + dynacast) para economia de banda.
      final room = livekit.Room(
        roomOptions: const livekit.RoomOptions(
          adaptiveStream: true,
          dynacast: true,
        ),
      );
      _room = room;
      _listener = room.createListener();

      _listener!
        ..on<livekit.ParticipantConnectedEvent>((_) => setState(() {}))
        ..on<livekit.ParticipantDisconnectedEvent>((_) => setState(() {}))
        ..on<livekit.TrackSubscribedEvent>((_) => setState(() {}))
        ..on<livekit.TrackUnsubscribedEvent>((_) => setState(() {}))
        ..on<livekit.TrackMutedEvent>((_) => setState(() {}))
        ..on<livekit.TrackUnmutedEvent>((_) => setState(() {}))
        ..on<livekit.ParticipantAttributesChanged>((_) => setState(() {}))
        ..on<livekit.ParticipantPermissionsUpdatedEvent>((_) {
          // Moderador liberou/bloqueou mic/câmera do cidadão.
          if (!_isModerator) _syncCitizenPublishPermissions();
          if (mounted) setState(() {});
        })
        ..on<livekit.RoomDisconnectedEvent>((_) {
          if (mounted) setState(() => _connectionState = 'closed');
        });

      await room
          .connect(_liveKitUrl, authToken)
          .timeout(
            const Duration(seconds: 15),
            onTimeout: () => throw Exception('Timeout ao conectar ao LiveKit'),
          );

      if (mounted) setState(() => _connectionState = 'connected');

      if (_isModerator) {
        _canPublishAudio = true;
        _canPublishVideo = true;
        _micOn = true;
        _camOn = true;
        try {
          await room.localParticipant?.setMicrophoneEnabled(true);
          await room.localParticipant?.setCameraEnabled(true);
        } catch (_) {}
      } else {
        // Cidadão entra apenas assistindo. Só publica depois que o moderador
        // liberar (evento de permissão) — não abre câmera/mic à toa aqui.
        _canPublishAudio = false;
        _canPublishVideo = false;
        _micOn = false;
        _camOn = false;
        _syncCitizenPublishPermissions();
      }
      if (mounted) setState(() {});
    } catch (_) {
      if (mounted) setState(() => _connectionState = 'denied');
    }
  }

  /// Lê as permissões de publicação concedidas pelo moderador (via LiveKit)
  /// e, ao ganhar permissão, publica automaticamente o track correspondente.
  void _syncCitizenPublishPermissions() {
    final local = _room?.localParticipant;
    if (local == null) return;
    final perms = local.permissions;

    final canPublish = perms.canPublish;
    // canPublishSources é a lista de fontes permitidas (enum protobuf).
    // Comparamos pelo nome para não depender do tipo interno.
    final sourceNames = perms.canPublishSources
        .map((s) => s.name.toUpperCase())
        .toList();
    // Se a lista está vazia mas canPublish=true, assume tudo liberado.
    final allowAll = canPublish && sourceNames.isEmpty;
    final audioAllowed =
        canPublish && (allowAll || sourceNames.contains('MICROPHONE'));
    final videoAllowed =
        canPublish && (allowAll || sourceNames.contains('CAMERA'));

    final gainedAudio = audioAllowed && !_canPublishAudio;
    final gainedVideo = videoAllowed && !_canPublishVideo;

    _canPublishAudio = audioAllowed;
    _canPublishVideo = videoAllowed;

    // Publica automaticamente ao receber a permissão (pede acesso ao HW aqui).
    if (gainedAudio) {
      _requestMediaPermissions().then((_) => _tryEnableMic());
    }
    if (gainedVideo) {
      _requestMediaPermissions().then((_) => _tryEnableCam());
    }
    if (!audioAllowed) {
      _micOn = false;
      unawaited(local.setMicrophoneEnabled(false));
    }
    if (!videoAllowed) {
      _camOn = false;
      unawaited(local.setCameraEnabled(false));
    }
    if (mounted) setState(() {});
  }

  Future<void> _tryEnableMic() async {
    try {
      await _room?.localParticipant?.setMicrophoneEnabled(true);
      _micOn = true;
      _canPublishAudio = true;
    } catch (_) {
      _canPublishAudio = false;
      _micOn = false;
    }
    if (mounted) setState(() {});
  }

  Future<void> _tryEnableCam() async {
    try {
      await _room?.localParticipant?.setCameraEnabled(true);
      _camOn = true;
      _canPublishVideo = true;
    } catch (_) {
      _canPublishVideo = false;
      _camOn = false;
    }
    if (mounted) setState(() {});
  }

  Future<void> _syncCitizenPermissions() async {
    if (_isModerator ||
        _connectionState != 'connected' ||
        _syncingPermissions) {
      return;
    }
    _syncingPermissions = true;
    try {
      final sala = await _salaService.getSalaById(widget.salaId);
      if (sala.status != 'OPEN') {
        await _room?.disconnect();
        if (mounted) setState(() => _connectionState = 'closed');
        _syncingPermissions = false;
        return;
      }
      final local = _room?.localParticipant;
      if (local == null) return;
      final audioAllowed =
          local.permissions.canPublish &&
          (local.permissions.canPublishSources.isEmpty ||
              local.permissions.canPublishSources.any(
                (s) => s.name.toUpperCase().contains('MICROPHONE'),
              ));
      final videoAllowed =
          local.permissions.canPublish &&
          (local.permissions.canPublishSources.isEmpty ||
              local.permissions.canPublishSources.any(
                (s) => s.name.toUpperCase().contains('CAMERA'),
              ));
      final hadPermission = _canPublishAudio || _canPublishVideo;
      final gainedAudio = audioAllowed && !_canPublishAudio;
      final gainedVideo = videoAllowed && !_canPublishVideo;
      _canPublishAudio = audioAllowed;
      _canPublishVideo = videoAllowed;
      if (!audioAllowed) _micOn = false;
      if (!videoAllowed) _camOn = false;
      if (!audioAllowed) {
        await _room?.localParticipant?.setMicrophoneEnabled(false);
      }
      if (!videoAllowed) await _room?.localParticipant?.setCameraEnabled(false);
      if (hadPermission && !audioAllowed && !videoAllowed) {
        await _room?.localParticipant?.setAttributes({
          'requestMic': 'false',
          'requestCam': 'false',
          'speechRequestId': '',
        });
        _requestingPermission = false;
      }
      if (gainedAudio) await _tryEnableMic();
      if (gainedVideo) await _tryEnableCam();
      if (gainedAudio || gainedVideo) {
        await _room?.localParticipant?.setAttributes({
          'requestMic': 'false',
          'requestCam': 'false',
          'speechRequestId': '',
        });
      }
      if (mounted) setState(() {});
    } catch (_) {
    } finally {
      _syncingPermissions = false;
    }
  }

  // ── Controles de mídia ─────────────────────────────────────

  Future<void> _toggleMic() async {
    if (!_canPublishAudio) return;
    _micOn = !_micOn;
    await _room?.localParticipant?.setMicrophoneEnabled(_micOn);
    if (mounted) setState(() {});
  }

  Future<void> _toggleCam() async {
    if (!_canPublishVideo) return;
    _camOn = !_camOn;
    await _room?.localParticipant?.setCameraEnabled(_camOn);
    if (mounted) setState(() {});
  }

  /// Cidadão sinaliza ao moderador que quer falar/ligar câmera,
  /// usando atributos do LiveKit (sem precisar de endpoint de backend).
  Future<void> _requestSpeakPermission() async {
    final room = _room;
    if (_requestingPermission) return;
    setState(() => _requestingPermission = true);
    _speechRequestId = DateTime.now().microsecondsSinceEpoch.toString();

    // 1) Registra a solicitação de fala no backend (persistente).
    try {
      await _salaService.solicitarFala(widget.salaId);
    } catch (_) {}

    // 2) Sinaliza em tempo real via atributos do LiveKit (redundância).
    try {
      await room?.localParticipant?.setAttributes({
        'requestMic': (!_canPublishAudio).toString(),
        'requestCam': (!_canPublishVideo).toString(),
        'speechRequestId': _speechRequestId,
      });
    } catch (_) {}
  }

  // ── Ações do moderador ─────────────────────────────────────

  Future<void> _loadPendingRequests() async {
    try {
      final reqs = await _salaService.getSolicitacoesEntrada(widget.salaId);
      final pending = reqs.where((r) => r.status == 'PENDING').toList();
      final names = <int, String>{};
      for (final req in pending) {
        try {
          final user = await _projectService.getUserById(req.userId);
          final name = (user.fullname?.isNotEmpty ?? false)
              ? user.fullname!
              : user.name;
          if (name.isNotEmpty) names[req.userId] = name;
        } catch (_) {}
      }
      if (mounted) {
        setState(() {
          _pendingRequests = pending;
          _requestNames
            ..clear()
            ..addAll(names);
        });
      }
      for (final req in reqs) {
        _mediaPermissions[req.userId] = (
          audio: req.canPublishAudio,
          video: req.canPublishVideo,
        );
      }
    } catch (_) {}
  }

  Future<void> _loadSpeakRequests() async {
    try {
      final reqs = await _salaService.getSolicitacoesFala(widget.salaId);
      final pending = reqs
          .where((r) => r.speechRequestStatus == 'PENDING')
          .toList();
      final names = <int, String>{};
      for (final req in pending) {
        if (_requestNames.containsKey(req.userId)) continue;
        try {
          final user = await _projectService.getUserById(req.userId);
          final name = (user.fullname?.isNotEmpty ?? false)
              ? user.fullname!
              : user.name;
          if (name.isNotEmpty) names[req.userId] = name;
        } catch (_) {}
      }
      if (mounted) {
        setState(() {
          _speakRequestIds = pending.map((r) => r.userId).toList();
          _requestNames.addAll(names);
        });
      }
    } catch (_) {}
  }

  Future<void> _aprovarFala(int userId) async {
    try {
      await _salaService.aprovarFala(widget.salaId, userId);
      // Garante que a permissão de publicação chegue ao participante no LiveKit.
      await _salaService.liberarMicrofone(widget.salaId, userId);
      await _salaService.liberarCamera(widget.salaId, userId);
    } catch (_) {
      // fallback: libera o microfone diretamente
      try {
        await _salaService.liberarMicrofone(widget.salaId, userId);
        await _salaService.liberarCamera(widget.salaId, userId);
      } catch (_) {}
    }
    if (mounted) {
      setState(() => _speakRequestIds.remove(userId));
    }
  }

  Future<void> _rejeitarFala(int userId) async {
    try {
      await _salaService.rejeitarFala(widget.salaId, userId);
    } catch (_) {}
    if (mounted) {
      setState(() => _speakRequestIds.remove(userId));
    }
  }

  Future<void> _approve(SolicitacaoEntrada req) async {
    try {
      await _salaService.aprovarSolicitacao(widget.salaId, req.userId);
      if (mounted) {
        setState(() => _pendingRequests.removeWhere((r) => r.id == req.id));
      }
    } catch (_) {}
  }

  Future<void> _deny(SolicitacaoEntrada req) async {
    try {
      await _salaService.rejeitarSolicitacao(widget.salaId, req.userId);
      if (mounted) {
        setState(() => _pendingRequests.removeWhere((r) => r.id == req.id));
      }
    } catch (_) {}
  }

  Future<void> _liberarMic(livekit.Participant p) async {
    try {
      final id = int.parse(p.identity);
      await _salaService.liberarMicrofone(widget.salaId, id);
      _mediaPermissions[id] = (
        audio: true,
        video: _mediaPermissions[id]?.video ?? false,
      );
      if (mounted) setState(() {});
    } catch (_) {}
  }

  Future<void> _revogarAcesso(livekit.Participant p) async {
    final id = int.parse(p.identity);
    if (mounted) setState(() => _revokingUserId = id);
    try {
      await _salaService.bloquearMicrofone(widget.salaId, id);
      await _salaService.bloquearCamera(widget.salaId, id);
      // Encerra o pedido APPROVED para que um novo clique gere PENDING.
      await _salaService.rejeitarFala(widget.salaId, id);
      _mediaPermissions[id] = (audio: false, video: false);
    } catch (_) {
    } finally {
      if (mounted) setState(() => _revokingUserId = null);
    }
  }

  Future<void> _encerrarSala() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Encerrar sala'),
        content: const Text('Encerrar esta sala para todos os participantes?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Encerrar'),
          ),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await _salaService.encerrarSala(widget.salaId);
      _room?.disconnect();
      if (mounted) Navigator.of(context).pop();
    } catch (_) {}
  }

  /// Cidadão sai da sala. Como não foi expulso, pode reentrar depois
  /// (o backend mantém a aprovação e gera novo token na reentrada).
  Future<void> _sairDaSala() async {
    _pollTimer?.cancel();
    await _room?.disconnect();
    if (mounted) Navigator.of(context).pop();
  }

  // ── Build ──────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0F1A),
      appBar: VoxAppBar(
        title: _sala?.name ?? 'Sala de audiência',
        actions: [
          if (_isModerator && _sala != null)
            IconButton(
              icon: const Icon(
                Icons.stop_circle_outlined,
                color: Colors.redAccent,
              ),
              tooltip: 'Encerrar sala',
              onPressed: _encerrarSala,
            ),
          if (!_isModerator)
            IconButton(
              icon: const Icon(Icons.logout, color: Colors.white70),
              tooltip: 'Sair da sala',
              onPressed: _sairDaSala,
            ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_loadError != null) {
      return Center(
        child: Text(_loadError!, style: const TextStyle(color: Colors.white)),
      );
    }
    if (_connectionState == 'requesting' || _connectionState == 'waiting') {
      return _buildWaitingScreen();
    }
    if (_connectionState == 'denied') {
      return _buildDeniedScreen();
    }
    if (_connectionState == 'closed') {
      return _buildClosedScreen();
    }
    if (_connectionState == 'connecting') {
      return _buildConnectingScreen();
    }
    if (_connectionState != 'connected') {
      return _buildConnectingScreen();
    }
    return _buildConnectedLayout();
  }

  Widget _buildConnectingScreen() {
    return Container(
      color: const Color(0xFF0F0F1A),
      child: const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              width: 36,
              height: 36,
              child: CircularProgressIndicator(
                strokeWidth: 3,
                color: Color(0xFFE8A838),
              ),
            ),
            SizedBox(height: 16),
            Text(
              'Conectando à sala...',
              style: TextStyle(color: Colors.white70, fontSize: 14),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildClosedScreen() {
    return Container(
      color: const Color(0xFF0F0F1A),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.stop_circle_outlined,
                size: 56,
                color: Colors.white38,
              ),
              const SizedBox(height: 16),
              const Text(
                'Sala encerrada',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Esta sala não está mais ativa.',
                style: TextStyle(color: Colors.white54, fontSize: 14),
              ),
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: () => Navigator.of(context).pop(),
                icon: const Icon(Icons.arrow_back),
                label: const Text('Voltar'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── Telas de estado ────────────────────────────────────────

  Widget _buildWaitingScreen() {
    return Container(
      color: const Color(0xFF0F0F1A),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E1E2E),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(
                  Icons.hourglass_top_rounded,
                  size: 56,
                  color: Color(0xFFE8A838),
                ),
              ),
              const SizedBox(height: 24),
              const Text(
                'Aguardando aprovação',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'O moderador precisa aprovar sua entrada na sala.\nVocê será conectado automaticamente.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 24),
              const SizedBox(
                width: 32,
                height: 32,
                child: CircularProgressIndicator(
                  strokeWidth: 3,
                  color: Color(0xFFE8A838),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDeniedScreen() {
    return Container(
      color: const Color(0xFF0F0F1A),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.block_rounded,
                size: 56,
                color: Colors.redAccent,
              ),
              const SizedBox(height: 16),
              const Text(
                'Entrada negada',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Não foi possível entrar na sala.\nSe você foi removido, peça entrada novamente.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 24),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  OutlinedButton.icon(
                    onPressed: () => Navigator.of(context).pop(),
                    icon: const Icon(Icons.arrow_back),
                    label: const Text('Voltar'),
                  ),
                  const SizedBox(width: 12),
                  FilledButton.icon(
                    onPressed: _retryEntry,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Tentar novamente'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── Layout principal (conectado) ───────────────────────────

  Widget _buildConnectedLayout() {
    return Column(
      children: [
        // Área de vídeo rolável (evita overflow quando a fila de aprovação
        // do moderador aparece — antes ela ficava fora da tela).
        Expanded(
          child: SingleChildScrollView(
            child: Column(
              children: [
                // Tile principal — moderador (câmera da câmara)
                _buildModeratorTile(),

                // Faixa de cidadãos
                _buildCitizensStrip(),
              ],
            ),
          ),
        ),

        // Controles
        _buildControls(),

        // Fila de aprovação (só moderador) — sempre visível no rodapé
        if (_isModerator) _buildApprovalSection(),
      ],
    );
  }

  // ── Tile do moderador (destaque) ───────────────────────────

  Widget _buildModeratorTile() {
    final mod = _moderatorParticipant;

    livekit.VideoTrack? videoTrack;
    if (mod != null) {
      for (final pub in mod.videoTrackPublications) {
        if (pub.track != null) {
          videoTrack = pub.track as livekit.VideoTrack;
          break;
        }
      }
    }

    final micPub = mod?.audioTrackPublications.isNotEmpty == true
        ? mod!.audioTrackPublications.first
        : null;
    final isMuted = micPub == null || micPub.muted;
    final name = mod != null
        ? (mod.name.isNotEmpty ? mod.name : mod.identity)
        : 'Aguardando moderador...';

    return Padding(
      padding: const EdgeInsets.fromLTRB(12, 12, 12, 6),
      // Quadro travado em 1:1 (quadrado) — evita vídeo esticado.
      child: Center(
        child: AspectRatio(
          aspectRatio: 1,
          child: Container(
            width: double.infinity,
            decoration: BoxDecoration(
              color: const Color(0xFF1A1A2E),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: const Color(0xFFE8A838).withValues(alpha: 0.6),
                width: 1.5,
              ),
            ),
            clipBehavior: Clip.antiAlias,
            child: Stack(
              fit: StackFit.expand,
              children: [
                // Vídeo ou avatar (cover = preenche o quadrado sem esticar)
                if (videoTrack != null)
                  livekit.VideoTrackRenderer(
                    videoTrack,
                    fit: livekit.VideoViewFit.cover,
                  )
                else
                  Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        CircleAvatar(
                          radius: 40,
                          backgroundColor: const Color(
                            0xFFE8A838,
                          ).withValues(alpha: 0.2),
                          child: Text(
                            mod != null
                                ? name.substring(0, 1).toUpperCase()
                                : '?',
                            style: const TextStyle(
                              fontSize: 32,
                              color: Color(0xFFE8A838),
                            ),
                          ),
                        ),
                        if (mod == null) ...[
                          const SizedBox(height: 12),
                          const Text(
                            'Aguardando moderador...',
                            style: TextStyle(
                              color: Colors.white54,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),

                // Badge "Câmara Municipal" no topo
                Positioned(
                  top: 10,
                  left: 12,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: const Color(0xFFE8A838),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.account_balance,
                          size: 12,
                          color: Colors.black,
                        ),
                        SizedBox(width: 4),
                        Text(
                          'Câmara Municipal',
                          style: TextStyle(
                            color: Colors.black,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

                // Nome + mic no rodapé
                Positioned(
                  bottom: 0,
                  left: 0,
                  right: 0,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 8,
                    ),
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.bottomCenter,
                        end: Alignment.topCenter,
                        colors: [Colors.black87, Colors.transparent],
                      ),
                    ),
                    child: Row(
                      children: [
                        Expanded(
                          child: Text(
                            name,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        Icon(
                          isMuted ? Icons.mic_off : Icons.mic,
                          size: 16,
                          color: isMuted
                              ? Colors.redAccent
                              : Colors.greenAccent,
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  // ── Faixa de cidadãos ──────────────────────────────────────

  Widget _buildCitizensStrip() {
    final citizens = _citizenParticipants;

    return SizedBox(
      height: 124,
      child: citizens.isEmpty
          ? const Center(
              child: Text(
                'Nenhum cidadão conectado',
                style: TextStyle(color: Colors.white38, fontSize: 12),
              ),
            )
          : ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12),
              itemCount: citizens.length,
              itemBuilder: (_, i) => _buildCitizenTile(citizens[i]),
            ),
    );
  }

  Widget _buildCitizenTile(livekit.Participant p) {
    livekit.VideoTrack? videoTrack;
    for (final pub in p.videoTrackPublications) {
      if (pub.track != null) {
        videoTrack = pub.track as livekit.VideoTrack;
        break;
      }
    }

    final micPub = p.audioTrackPublications.isNotEmpty
        ? p.audioTrackPublications.first
        : null;
    final isMicMuted = micPub == null || micPub.muted;
    final name = p.name.isNotEmpty ? p.name : p.identity;
    final isLocal = p is livekit.LocalParticipant;

    // Verifica se este participante pediu permissão para falar (via atributos)
    final attrs = p.attributes;
    final wantsMic = attrs['requestMic'] == 'true';
    final wantsCam = attrs['requestCam'] == 'true';
    final wantsPermission = wantsMic || wantsCam;

    return GestureDetector(
      onLongPress: null,
      child: Container(
        // Quadro travado em 1:1 (quadrado): largura = altura da faixa menos margens.
        width: 116,
        margin: const EdgeInsets.only(right: 8, top: 4, bottom: 4),
        decoration: BoxDecoration(
          color: const Color(0xFF1A1A2E),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: wantsPermission
                ? Colors.orangeAccent
                : (isLocal
                      ? Colors.blueAccent.withValues(alpha: 0.6)
                      : Colors.white12),
            width: wantsPermission ? 2 : 1,
          ),
        ),
        clipBehavior: Clip.antiAlias,
        child: Stack(
          fit: StackFit.expand,
          children: [
            if (videoTrack != null)
              livekit.VideoTrackRenderer(
                videoTrack,
                fit: livekit.VideoViewFit.cover,
              )
            else
              Center(
                child: CircleAvatar(
                  radius: 22,
                  backgroundColor: isLocal
                      ? Colors.blueAccent.withValues(alpha: 0.3)
                      : Colors.white12,
                  child: Text(
                    name.substring(0, 1).toUpperCase(),
                    style: const TextStyle(fontSize: 18, color: Colors.white),
                  ),
                ),
              ),

            // Ícone de "quer falar" (só visível para moderador)
            if (wantsPermission && _isModerator)
              Positioned(
                top: 4,
                right: 4,
                child: Container(
                  padding: const EdgeInsets.all(3),
                  decoration: const BoxDecoration(
                    color: Colors.orangeAccent,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.record_voice_over,
                    size: 10,
                    color: Colors.black,
                  ),
                ),
              ),

            // Nome + mic no rodapé
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: Container(
                color: Colors.black54,
                padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 3),
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        isLocal ? 'Você' : name,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Icon(
                      isMicMuted ? Icons.mic_off : Icons.mic,
                      size: 10,
                      color: isMicMuted ? Colors.redAccent : Colors.greenAccent,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Controles de mídia ─────────────────────────────────────

  Widget _buildControls() {
    // Cidadão sem permissão vê botão de "pedir para falar"
    if (!_isModerator && !_canPublishAudio && !_canPublishVideo) {
      return _buildBlockedControls();
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _ControlButton(
            icon: _micOn ? Icons.mic : Icons.mic_off,
            label: _micOn ? 'Mudo' : 'Ativar mic',
            active: _micOn,
            enabled: _canPublishAudio,
            onTap: _canPublishAudio ? _toggleMic : null,
          ),
          const SizedBox(width: 20),
          _ControlButton(
            icon: _camOn ? Icons.videocam : Icons.videocam_off,
            label: _camOn ? 'Câmera' : 'Ativar cam',
            active: _camOn,
            enabled: _canPublishVideo,
            onTap: _canPublishVideo ? _toggleCam : null,
          ),
        ],
      ),
    );
  }

  /// Área exibida ao cidadão quando mic e câmera estão bloqueados
  Widget _buildBlockedControls() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF1E1E2E),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white12),
      ),
      child: Row(
        children: [
          const Icon(Icons.lock_outline, color: Colors.white38, size: 20),
          const SizedBox(width: 10),
          const Expanded(
            child: Text(
              'Microfone e câmera bloqueados pelo moderador',
              style: TextStyle(color: Colors.white54, fontSize: 12),
            ),
          ),
          const SizedBox(width: 10),
          TextButton.icon(
            style: TextButton.styleFrom(
              backgroundColor: _requestingPermission
                  ? Colors.white12
                  : const Color(0xFFE8A838).withValues(alpha: 0.2),
              foregroundColor: const Color(0xFFE8A838),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            onPressed: _requestingPermission ? null : _requestSpeakPermission,
            icon: const Icon(Icons.record_voice_over, size: 16),
            label: Text(
              _requestingPermission ? 'Pedido enviado' : 'Pedir para falar',
              style: const TextStyle(fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }

  // ── Seção de aprovações (moderador) ────────────────────────

  Widget _buildApprovalSection() {
    // Participantes conectados que têm atributo requestMic ou requestCam
    final room = _room;
    final requesters = <livekit.Participant>[];
    if (room != null) {
      for (final p in room.remoteParticipants.values) {
        final attrs = p.attributes;
        if (attrs['requestMic'] == 'true' ||
            attrs['requestCam'] == 'true' ||
            (attrs['speechRequestId']?.isNotEmpty ?? false)) {
          requesters.add(p);
        }
      }
    }

    final hasPending = _pendingRequests.isNotEmpty;
    final hasRequesters = requesters.isNotEmpty;
    final hasSpeakRequests = _speakRequestIds.isNotEmpty;
    final participants = _citizenParticipants;

    if (!hasPending &&
        !hasRequesters &&
        !hasSpeakRequests &&
        participants.isEmpty) {
      return const SizedBox.shrink();
    }

    return Container(
      constraints: const BoxConstraints(maxHeight: 220),
      decoration: const BoxDecoration(
        color: Color(0xFF12121F),
        border: Border(top: BorderSide(color: Colors.white12)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          if (participants.isNotEmpty) ...[
            const Padding(
              padding: EdgeInsets.fromLTRB(16, 10, 16, 4),
              child: Text(
                'Participantes',
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            ...participants.map((p) {
              final name = p.name.isNotEmpty ? p.name : p.identity;
              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 16,
                      backgroundColor: Colors.white12,
                      child: Text(
                        name.substring(0, 1).toUpperCase(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 13,
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    SizedBox(
                      width: 92,
                      child: Text(
                        name,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 13,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Expanded(
                      child: Center(
                        child: IconButton(
                          tooltip: 'Revogar acesso ao microfone e câmera',
                          icon: Icon(
                            _revokingUserId == int.tryParse(p.identity)
                                ? Icons.hourglass_top
                                : Icons.lock_outline,
                            color: Colors.redAccent,
                            size: 20,
                          ),
                          onPressed: _revokingUserId == int.tryParse(p.identity)
                              ? null
                              : () => _revogarAcesso(p),
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }),
          ],

          // Pedidos de entrada (PENDING)
          if (hasPending) ...[
            const Padding(
              padding: EdgeInsets.fromLTRB(16, 10, 16, 4),
              child: Text(
                'Pedidos de entrada',
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            ...(_pendingRequests.map((req) {
              final name =
                  _requestNames[req.userId] ?? 'Usuário #${req.userId}';
              return ListTile(
                dense: true,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                leading: CircleAvatar(
                  radius: 16,
                  backgroundColor: Colors.blueAccent.withValues(alpha: 0.2),
                  child: Text(
                    name.substring(0, 1).toUpperCase(),
                    style: const TextStyle(color: Colors.white, fontSize: 13),
                  ),
                ),
                title: Text(
                  name,
                  style: const TextStyle(color: Colors.white, fontSize: 13),
                ),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _ActionChip(
                      label: 'Aprovar',
                      color: Colors.greenAccent,
                      onTap: () => _approve(req),
                    ),
                    const SizedBox(width: 8),
                    _ActionChip(
                      label: 'Negar',
                      color: Colors.redAccent,
                      onTap: () => _deny(req),
                    ),
                  ],
                ),
              );
            })),
          ],

          // Pedidos de fala registrados no backend
          if (hasSpeakRequests) ...[
            const Padding(
              padding: EdgeInsets.fromLTRB(16, 10, 16, 4),
              child: Text(
                'Pedidos de fala',
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            ...(_speakRequestIds.map((userId) {
              final name = _requestNames[userId] ?? 'Usuário #$userId';
              return ListTile(
                dense: true,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                leading: CircleAvatar(
                  radius: 16,
                  backgroundColor: Colors.orangeAccent.withValues(alpha: 0.2),
                  child: Text(
                    name.substring(0, 1).toUpperCase(),
                    style: const TextStyle(color: Colors.white, fontSize: 13),
                  ),
                ),
                title: Text(
                  name,
                  style: const TextStyle(color: Colors.white, fontSize: 13),
                ),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _ActionChip(
                      label: 'Aprovar',
                      color: Colors.greenAccent,
                      onTap: () => _aprovarFala(userId),
                    ),
                    const SizedBox(width: 8),
                    _ActionChip(
                      label: 'Recusar',
                      color: Colors.redAccent,
                      onTap: () => _rejeitarFala(userId),
                    ),
                  ],
                ),
              );
            })),
          ],

          // Pedidos de mic/câmera de quem já está na sala
          if (hasRequesters) ...[
            const Padding(
              padding: EdgeInsets.fromLTRB(16, 10, 16, 4),
              child: Text(
                'Pedidos de microfone / câmera',
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            ...(requesters.map((p) {
              final name = p.name.isNotEmpty ? p.name : p.identity;
              final wantsMic = p.attributes['requestMic'] == 'true';
              final wantsCam = p.attributes['requestCam'] == 'true';
              return ListTile(
                dense: true,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                leading: CircleAvatar(
                  radius: 16,
                  backgroundColor: Colors.orangeAccent.withValues(alpha: 0.2),
                  child: Text(
                    name.substring(0, 1).toUpperCase(),
                    style: const TextStyle(color: Colors.white, fontSize: 13),
                  ),
                ),
                title: Text(
                  name,
                  style: const TextStyle(color: Colors.white, fontSize: 13),
                ),
                subtitle: Row(
                  children: [
                    if (wantsMic)
                      const _MediaBadge(icon: Icons.mic, label: 'Mic'),
                    if (wantsMic && wantsCam) const SizedBox(width: 6),
                    if (wantsCam)
                      const _MediaBadge(icon: Icons.videocam, label: 'Câmera'),
                  ],
                ),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (wantsMic)
                      _ActionChip(
                        label: 'Liberar mic',
                        color: Colors.greenAccent,
                        onTap: () => _liberarMic(p),
                      ),
                    if (wantsMic && wantsCam) const SizedBox(width: 8),
                    if (wantsCam)
                      _ActionChip(
                        label: 'Liberar cam',
                        color: Colors.greenAccent,
                        onTap: () async {
                          try {
                            await _salaService.liberarCamera(
                              widget.salaId,
                              int.parse(p.identity),
                            );
                          } catch (_) {}
                        },
                      ),
                  ],
                ),
              );
            })),
          ],
        ],
      ),
    );
  }
}

// ── Widgets auxiliares ─────────────────────────────────────────

class _ControlButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool active;
  final bool enabled;
  final VoidCallback? onTap;

  const _ControlButton({
    required this.icon,
    required this.label,
    required this.active,
    required this.enabled,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final color = !enabled
        ? Colors.white24
        : active
        ? Colors.white
        : Colors.redAccent;

    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 52,
            height: 52,
            decoration: BoxDecoration(
              color: !enabled
                  ? Colors.white.withValues(alpha: 0.05)
                  : active
                  ? Colors.white.withValues(alpha: 0.15)
                  : Colors.redAccent.withValues(alpha: 0.2),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 22),
          ),
          const SizedBox(height: 4),
          Text(label, style: TextStyle(color: color, fontSize: 10)),
        ],
      ),
    );
  }
}

class _ActionChip extends StatelessWidget {
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _ActionChip({
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.15),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color.withValues(alpha: 0.5)),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: color,
            fontSize: 11,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _MediaBadge extends StatelessWidget {
  final IconData icon;
  final String label;

  const _MediaBadge({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 11, color: Colors.orangeAccent),
        const SizedBox(width: 3),
        Text(
          label,
          style: const TextStyle(color: Colors.orangeAccent, fontSize: 10),
        ),
      ],
    );
  }
}
