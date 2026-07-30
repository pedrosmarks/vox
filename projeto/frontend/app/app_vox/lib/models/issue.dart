class IssueReport {
  final int id;
  final String title;
  final String description;
  final int municipalityId;
  final int categoryId;
  final String status;
  final int authorId;
  final String createdAt;
  final String updatedAt;
  final String neighborhood;
  final String street;
  final String number;
  final double latitude;
  final double longitude;

  IssueReport({
    required this.id,
    required this.title,
    required this.description,
    required this.municipalityId,
    required this.categoryId,
    required this.status,
    required this.authorId,
    required this.createdAt,
    required this.updatedAt,
    required this.neighborhood,
    required this.street,
    required this.number,
    required this.latitude,
    required this.longitude,
  });

  factory IssueReport.fromJson(Map<String, dynamic> json) => IssueReport(
        id: json['id'] as int,
        title: json['title'] as String? ?? '',
        description: json['description'] as String? ?? '',
        municipalityId: json['municipalityId'] as int? ?? 0,
        categoryId: json['categoryId'] as int? ?? 0,
        status: json['status'] as String? ?? '',
        authorId: json['authorId'] as int? ?? 0,
        createdAt: json['createdAt'] as String? ?? '',
        updatedAt: json['updatedAt'] as String? ?? '',
        neighborhood: json['neighborhood'] as String? ?? '',
        street: json['street'] as String? ?? '',
        number: json['number'] as String? ?? '',
        latitude: (json['latitude'] as num?)?.toDouble() ?? 0,
        longitude: (json['longitude'] as num?)?.toDouble() ?? 0,
      );
}

class IssueImage {
  final int id;
  final int issueId;
  final String url;

  IssueImage({required this.id, required this.issueId, required this.url});

  factory IssueImage.fromJson(Map<String, dynamic> json) => IssueImage(
        id: json['id'] as int,
        issueId: json['issueId'] as int,
        url: json['url'] as String,
      );
}

class IssueStatusHistory {
  final int id;
  final int issueId;
  final String status;
  final String changedAt;

  IssueStatusHistory({
    required this.id,
    required this.issueId,
    required this.status,
    required this.changedAt,
  });

  factory IssueStatusHistory.fromJson(Map<String, dynamic> json) =>
      IssueStatusHistory(
        id: json['id'] as int,
        issueId: json['issueId'] as int,
        status: json['status'] as String,
        changedAt: json['changedAt'] as String,
      );
}
