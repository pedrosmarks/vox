class UserProfile {
  final int id;
  final String email;
  final String name;
  final String? fullname;
  final String role;
  final int municipalityId;
  final String? phone;
  final String? cpf;
  final String? birthDate;

  UserProfile({
    required this.id,
    required this.email,
    required this.name,
    this.fullname,
    required this.role,
    required this.municipalityId,
    this.phone,
    this.cpf,
    this.birthDate,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) => UserProfile(
        id: json['id'] as int,
        email: json['email'] as String,
        name: json['name'] as String,
        fullname: json['fullname'] as String?,
        role: json['role'] as String,
        municipalityId: json['municipalityId'] as int? ?? 0,
        phone: json['phone'] as String?,
        cpf: json['cpf'] as String?,
        birthDate: json['birthDate'] as String?,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'email': email,
        'name': name,
        if (fullname != null) 'fullname': fullname,
        'role': role,
        'municipalityId': municipalityId,
        if (phone != null) 'phone': phone,
        if (cpf != null) 'cpf': cpf,
        if (birthDate != null) 'birthDate': birthDate,
      };
}

/// Representação reduzida de usuário, usada em referências (autor, etc).
class UserSummary {
  final int id;
  final String name;
  final String? fullname;

  UserSummary({required this.id, required this.name, this.fullname});

  factory UserSummary.fromJson(Map<String, dynamic> json) => UserSummary(
        id: json['id'] as int,
        name: json['name'] as String,
        fullname: json['fullname'] as String?,
      );
}
