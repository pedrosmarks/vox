import 'package:flutter/material.dart';
import '../models/event.dart';
import '../services/auth_service.dart';
import '../services/event_service.dart';
import '../services/municipality_service.dart';

class EventosScreen extends StatefulWidget {
  const EventosScreen({super.key});
  @override
  State<EventosScreen> createState() => _EventosScreenState();
}

class _EventosScreenState extends State<EventosScreen> {
  final service = EventService();
  final auth = AuthService();
  final municipalityService = MunicipalityService();
  final search = TextEditingController();
  List<CivicEvent> events = [];
  bool free = false, loading = true;
  String? error;
  bool isModerator = false;
  List<Map<String, dynamic>> categories = [];
  List<Municipality> municipalities = [];
  int? selectedMunicipalityId;
  @override
  void initState() {
    super.initState();
    _loadPermissions();
    load();
  }

  Future<void> _loadPermissions() async {
    final role = await auth.getUserRole();
    selectedMunicipalityId = await auth.getMunicipalityId();
    try {
      municipalities = await municipalityService.getMunicipalities();
    } catch (_) {}
    if (!mounted) return;
    setState(() => isModerator = role == 'MODERATOR');
    if (isModerator) {
      try {
        categories = await service.getCategories();
      } catch (_) {}
      if (mounted) setState(() {});
    }
  }

  Future<void> load() async {
    setState(() {
      loading = true;
      error = null;
    });
    try {
      final result = await service.getEvents(
        search: search.text,
        free: free,
        municipalityId: selectedMunicipalityId,
      );
      final loaded = result.content;
      final withImages = await Future.wait(
        loaded.map((event) async {
          try {
            final images = await service.getEventImages(event.id);
            return CivicEvent(
              id: event.id,
              title: event.title,
              description: event.description,
              categoryId: event.categoryId,
              price: event.price,
              startDate: event.startDate,
              endDate: event.endDate,
              location: event.location,
              images: images,
            );
          } catch (_) {
            return event;
          }
        }),
      );
      if (mounted) setState(() => events = withImages);
    } catch (_) {
      if (mounted) {
        setState(() => error = 'Não foi possível carregar os eventos.');
      }
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Eventos')),
      floatingActionButton: isModerator
          ? FloatingActionButton.extended(
              onPressed: () => _openForm(),
              icon: const Icon(Icons.add),
              label: const Text('Novo evento'),
            )
          : null,
      body: RefreshIndicator(
        onRefresh: load,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: search,
                    onSubmitted: (_) => load(),
                    decoration: const InputDecoration(
                      labelText: 'Buscar eventos',
                      prefixIcon: Icon(Icons.search),
                    ),
                  ),
                ),
                IconButton(
                  onPressed: () {
                    setState(() => free = !free);
                    load();
                  },
                  icon: Icon(
                    free ? Icons.check_box : Icons.check_box_outline_blank,
                  ),
                  tooltip: 'Somente gratuitos',
                ),
              ],
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<int?>(
              initialValue: selectedMunicipalityId,
              decoration: const InputDecoration(
                labelText: 'Município',
                prefixIcon: Icon(Icons.location_city_outlined),
              ),
              items: [
                const DropdownMenuItem<int?>(
                  value: null,
                  child: Text('Todos os municípios'),
                ),
                ...municipalities.map(
                  (municipality) => DropdownMenuItem<int?>(
                    value: municipality.id,
                    child: Text(municipality.label),
                  ),
                ),
              ],
              onChanged: (value) {
                setState(() => selectedMunicipalityId = value);
                load();
              },
            ),
            const SizedBox(height: 16),
            if (loading)
              const Center(child: CircularProgressIndicator())
            else if (error != null)
              Center(child: Text(error!))
            else if (events.isEmpty)
              const Center(child: Text('Nenhum evento encontrado.'))
            else
              ...events.map(
                (event) => Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  clipBehavior: Clip.antiAlias,
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(12),
                    leading: event.images.isNotEmpty
                        ? Image.network(
                            event.images.first.url,
                            width: 96,
                            height: 96,
                            fit: BoxFit.cover,
                          )
                        : SizedBox(
                            width: 96,
                            height: 96,
                            child: ColoredBox(
                              color: Theme.of(
                                context,
                              ).colorScheme.surfaceContainerHighest,
                              child: Icon(Icons.event),
                            ),
                          ),
                    title: Text(
                      event.title,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                    ),
                    subtitle: Text(
                      '${event.startDate != null ? _date(event.startDate!) : 'Data a confirmar'}\n${event.location.isEmpty ? 'Local a confirmar' : event.location}\n${event.price != null && event.price! > 0 ? 'R\$ ${event.price!.toStringAsFixed(2)}' : 'Gratuito'}',
                      maxLines: 2,
                    ),
                    isThreeLine: true,
                    trailing: isModerator
                        ? PopupMenuButton<String>(
                            onSelected: (value) {
                              if (value == 'edit') _openForm(event);
                              if (value == 'delete') _deleteEvent(event);
                            },
                            itemBuilder: (_) => const [
                              PopupMenuItem(
                                value: 'edit',
                                child: Text('Editar'),
                              ),
                              PopupMenuItem(
                                value: 'delete',
                                child: Text('Excluir'),
                              ),
                            ],
                          )
                        : null,
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => EventoDetalheScreen(id: event.id),
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Future<void> _deleteEvent(CivicEvent event) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Excluir evento?'),
        content: Text('O evento "${event.title}" será removido.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Excluir'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await service.deleteEvent(event.id);
      await load();
    } catch (_) {
      if (mounted) setState(() => error = 'Não foi possível excluir o evento.');
    }
  }

  Future<void> _openForm([CivicEvent? event]) async {
    final title = TextEditingController(text: event?.title ?? '');
    final description = TextEditingController(text: event?.description ?? '');
    final category = TextEditingController(
      text: event?.categoryId.toString() ?? '',
    );
    final price = TextEditingController(text: event?.price?.toString() ?? '');
    final location = TextEditingController(text: event?.location ?? '');
    final start = TextEditingController(
      text: event?.startDate?.toIso8601String().substring(0, 19) ?? '',
    );
    final end = TextEditingController(
      text: event?.endDate?.toIso8601String().substring(0, 19) ?? '',
    );
    final formKey = GlobalKey<FormState>();
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(event == null ? 'Novo evento' : 'Editar evento'),
        content: SizedBox(
          width: 520,
          child: SingleChildScrollView(
            child: Form(
              key: formKey,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextFormField(
                    controller: title,
                    decoration: const InputDecoration(labelText: 'Título *'),
                    validator: (v) => v == null || v.trim().isEmpty
                        ? 'Informe o título'
                        : null,
                  ),
                  TextFormField(
                    controller: category,
                    decoration: const InputDecoration(
                      labelText: 'ID da categoria *',
                    ),
                    keyboardType: TextInputType.number,
                    validator: (v) => int.tryParse(v ?? '') == null
                        ? 'Informe a categoria'
                        : null,
                  ),
                  TextFormField(
                    controller: description,
                    decoration: const InputDecoration(labelText: 'Descrição'),
                    maxLines: 3,
                  ),
                  TextFormField(
                    controller: price,
                    decoration: const InputDecoration(labelText: 'Preço'),
                    keyboardType: const TextInputType.numberWithOptions(
                      decimal: true,
                    ),
                  ),
                  TextFormField(
                    controller: location,
                    decoration: const InputDecoration(labelText: 'Local'),
                  ),
                  TextFormField(
                    controller: start,
                    decoration: const InputDecoration(
                      labelText: 'Início (AAAA-MM-DDTHH:MM:SS)',
                    ),
                  ),
                  TextFormField(
                    controller: end,
                    decoration: const InputDecoration(
                      labelText: 'Fim (AAAA-MM-DDTHH:MM:SS)',
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              if (formKey.currentState?.validate() != true) return;
              final fields = <String, String>{
                'title': title.text.trim(),
                'categoryId': category.text.trim(),
                'description': description.text.trim(),
                'price': price.text.trim(),
                'startDate': start.text.trim(),
                'endDate': end.text.trim(),
                'location': location.text.trim(),
              };
              try {
                if (event == null) {
                  await service.createEvent(fields);
                } else {
                  await service.updateEvent(event.id, fields);
                }
                if (dialogContext.mounted) Navigator.pop(dialogContext, true);
              } catch (_) {
                if (dialogContext.mounted) {
                  ScaffoldMessenger.of(dialogContext).showSnackBar(
                    const SnackBar(
                      content: Text('Não foi possível salvar o evento.'),
                    ),
                  );
                }
              }
            },
            child: const Text('Salvar'),
          ),
        ],
      ),
    );
    for (final controller in [
      title,
      description,
      category,
      price,
      location,
      start,
      end,
    ]) {
      controller.dispose();
    }
    if (saved == true) await load();
  }

  static String _date(DateTime date) =>
      '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
}

class EventoDetalheScreen extends StatelessWidget {
  final int id;
  const EventoDetalheScreen({super.key, required this.id});
  @override
  Widget build(BuildContext context) => FutureBuilder<CivicEvent>(
    future: EventService().getEvent(id),
    builder: (context, snapshot) {
      if (!snapshot.hasData) {
        return Scaffold(
          appBar: AppBar(),
          body: Center(
            child: snapshot.hasError
                ? const Text('Evento não encontrado.')
                : const CircularProgressIndicator(),
          ),
        );
      }
      final event = snapshot.data!;
      return Scaffold(
        appBar: AppBar(title: const Text('Detalhes do evento')),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            if (event.images.isNotEmpty)
              SizedBox(
                height: 220,
                child: PageView(
                  children: event.images
                      .map(
                        (image) => Image.network(image.url, fit: BoxFit.cover),
                      )
                      .toList(),
                ),
              ),
            const SizedBox(height: 20),
            Text(event.title, style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 12),
            Text(
              event.description.isEmpty
                  ? 'Sem descrição disponível.'
                  : event.description,
            ),
            const SizedBox(height: 24),
            Text(
              'Quando: ${event.startDate == null ? 'A confirmar' : _EventosScreenState._date(event.startDate!)}',
            ),
            Text(
              'Onde: ${event.location.isEmpty ? 'Local a confirmar' : event.location}',
            ),
            Text(
              'Entrada: ${event.price != null && event.price! > 0 ? 'R\$ ${event.price!.toStringAsFixed(2)}' : 'Gratuita'}',
            ),
          ],
        ),
      );
    },
  );
}
