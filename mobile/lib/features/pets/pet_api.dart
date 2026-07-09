import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/api/api_client.dart';

class PetResult {
  const PetResult({
    required this.petId,
    required this.ownerId,
    required this.name,
    required this.type,
    required this.photoPath,
  });

  final int petId;
  final int ownerId;
  final String name;
  final String type;
  final String photoPath;

  factory PetResult.fromJson(Map<String, dynamic> json) {
    return PetResult(
      petId: json['petId'] as int,
      ownerId: json['ownerId'] as int,
      name: json['name'] as String,
      type: json['type'] as String,
      photoPath: json['photoPath'] as String,
    );
  }
}

class PetApi {
  PetApi(this._dio);

  final Dio _dio;

  Future<PetResult> createPet({
    required int ownerId,
    required String name,
    required String type,
    required XFile photo,
  }) async {
    final formData = FormData.fromMap({
      'ownerId': ownerId,
      'name': name,
      'type': type,
      'photo': await MultipartFile.fromFile(photo.path, filename: photo.name),
    });

    final response = await _dio.post<Map<String, dynamic>>(
      '/api/pets',
      data: formData,
      options: Options(contentType: 'multipart/form-data'),
    );
    return PetResult.fromJson(response.data!);
  }
}

final petApiProvider = Provider<PetApi>(
  (ref) => PetApi(ref.watch(dioProvider)),
);
