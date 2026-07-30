class Project {
  final int id;
  final int municipalityId;
  final int categoryId;
  final String type;
  final String title;
  final String description;
  final String status;
  final int authorId;
  final String createdAt;
  final String updatedAt;
  final bool highlighted;
  final bool isOfficial;
  final String neighborhood;
  final String street;
  final String number;
  final double latitude;
  final double longitude;
  final String startDate;
  final String expectedEndDate;
  final String? endDate;
  final String? financialAnalysis;
  final double estimatedCost;
  final double approvedBudget;

  Project({
    required this.id,
    required this.municipalityId,
    required this.categoryId,
    required this.type,
    required this.title,
    required this.description,
    required this.status,
    required this.authorId,
    required this.createdAt,
    required this.updatedAt,
    required this.highlighted,
    required this.isOfficial,
    required this.neighborhood,
    required this.street,
    required this.number,
    required this.latitude,
    required this.longitude,
    required this.startDate,
    required this.expectedEndDate,
    this.endDate,
    this.financialAnalysis,
    required this.estimatedCost,
    required this.approvedBudget,
  });

  factory Project.fromJson(Map<String, dynamic> json) => Project(
        id: json['id'] as int,
        municipalityId: json['municipalityId'] as int? ?? 0,
        categoryId: json['categoryId'] as int? ?? 0,
        type: json['type'] as String? ?? '',
        title: json['title'] as String? ?? '',
        description: json['description'] as String? ?? '',
        status: json['status'] as String? ?? '',
        authorId: json['authorId'] as int? ?? 0,
        createdAt: json['createdAt'] as String? ?? '',
        updatedAt: json['updatedAt'] as String? ?? '',
        highlighted: json['highlighted'] as bool? ?? false,
        isOfficial: json['isOfficial'] as bool? ?? false,
        neighborhood: json['neighborhood'] as String? ?? '',
        street: json['street'] as String? ?? '',
        number: json['number'] as String? ?? '',
        latitude: (json['latitude'] as num?)?.toDouble() ?? 0,
        longitude: (json['longitude'] as num?)?.toDouble() ?? 0,
        startDate: json['startDate'] as String? ?? '',
        expectedEndDate: json['expectedEndDate'] as String? ?? '',
        endDate: json['endDate'] as String?,
        financialAnalysis: json['financialAnalysis'] as String?,
        estimatedCost: (json['estimatedCost'] as num?)?.toDouble() ?? 0,
        approvedBudget: (json['approvedBudget'] as num?)?.toDouble() ?? 0,
      );
}

class Category {
  final int id;
  final String name;

  const Category({required this.id, required this.name});

  factory Category.fromJson(Map<String, dynamic> json) => Category(
        id: json['id'] as int,
        name: json['name'] as String,
      );
}

class ProjectImage {
  final int id;
  final int projectId;
  final String url;

  ProjectImage({required this.id, required this.projectId, required this.url});

  factory ProjectImage.fromJson(Map<String, dynamic> json) => ProjectImage(
        id: json['id'] as int,
        projectId: json['projectId'] as int,
        url: json['url'] as String,
      );
}
