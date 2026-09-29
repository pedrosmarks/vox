class EventImage {
  final int id;
  final String url;
  const EventImage({required this.id, required this.url});
  factory EventImage.fromJson(Map<String, dynamic> json) => EventImage(
    id: json['id'] as int? ?? 0,
    url: json['url'] as String? ?? '',
  );
}

class CivicEvent {
  final int id;
  final String title;
  final String description;
  final int categoryId;
  final double? price;
  final DateTime? startDate;
  final DateTime? endDate;
  final String neighborhood;
  final String street;
  final String number;
  final double? latitude;
  final double? longitude;
  final String location;
  final List<EventImage> images;
  const CivicEvent({
    required this.id,
    required this.title,
    required this.description,
    required this.categoryId,
    this.price,
    this.startDate,
    this.endDate,
    this.neighborhood = '',
    this.street = '',
    this.number = '',
    this.latitude,
    this.longitude,
    required this.location,
    this.images = const [],
  });
  factory CivicEvent.fromJson(Map<String, dynamic> json) => CivicEvent(
    id: json['id'] as int? ?? 0,
    title: json['title'] as String? ?? '',
    description: json['description'] as String? ?? '',
    categoryId: json['categoryId'] as int? ?? 0,
    price: (json['price'] as num?)?.toDouble(),
    startDate: DateTime.tryParse(json['startDate'] as String? ?? ''),
    endDate: DateTime.tryParse(json['endDate'] as String? ?? ''),
    neighborhood: json['neighborhood'] as String? ?? '',
    street: json['street'] as String? ?? '',
    number: json['number'] as String? ?? '',
    latitude: (json['latitude'] as num?)?.toDouble(),
    longitude: (json['longitude'] as num?)?.toDouble(),
    location: _formatLocation(json),
    images: (json['images'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(EventImage.fromJson)
        .toList(),
  );

  static String _formatLocation(Map<String, dynamic> json) {
    final parts = [
      json['street'] as String?,
      json['number'] as String?,
      json['neighborhood'] as String?,
    ].where((part) => part != null && part.trim().isNotEmpty).toList();
    return parts.isEmpty ? json['location'] as String? ?? '' : parts.join(', ');
  }
}

class EventPage {
  final List<CivicEvent> content;
  final int totalElements;
  const EventPage({required this.content, required this.totalElements});
  factory EventPage.fromJson(Map<String, dynamic> json) => EventPage(
    content: (json['content'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(CivicEvent.fromJson)
        .toList(),
    totalElements: json['totalElements'] as int? ?? 0,
  );
}
