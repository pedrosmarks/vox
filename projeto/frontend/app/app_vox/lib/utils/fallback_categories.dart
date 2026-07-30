import '../models/project.dart';

/// Categorias usadas quando a API `/api/categories` não responder.
const List<Category> fallbackCategories = [
  Category(id: 1, name: 'Infraestrutura'),
  Category(id: 2, name: 'Saúde'),
  Category(id: 3, name: 'Educação'),
  Category(id: 4, name: 'Transporte'),
  Category(id: 5, name: 'Meio Ambiente'),
  Category(id: 6, name: 'Cultura e Lazer'),
  Category(id: 7, name: 'Segurança Pública'),
];
