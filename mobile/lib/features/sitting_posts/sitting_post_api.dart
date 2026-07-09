import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';

class SittingPostResult {
  const SittingPostResult({
    required this.postId,
    required this.ownerId,
    required this.petId,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.status,
    this.assignedSitterId,
    this.distanceKm,
  });

  final int postId;
  final int ownerId;
  final int petId;
  final String description;
  final double latitude;
  final double longitude;
  final String status;
  final int? assignedSitterId;
  final double? distanceKm;

  factory SittingPostResult.fromJson(Map<String, dynamic> json) {
    return SittingPostResult(
      postId: json['postId'] as int,
      ownerId: json['ownerId'] as int,
      petId: json['petId'] as int,
      description: json['description'] as String,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      status: json['status'] as String,
      assignedSitterId: json['assignedSitterId'] as int?,
      distanceKm: (json['distanceKm'] as num?)?.toDouble(),
    );
  }
}

class SittingPostApi {
  SittingPostApi(this._dio);

  final Dio _dio;

  Future<SittingPostResult> createPost({
    required int ownerId,
    required int petId,
    required String description,
    required double latitude,
    required double longitude,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/api/sitting-posts',
      data: {
        'ownerId': ownerId,
        'petId': petId,
        'description': description,
        'latitude': latitude,
        'longitude': longitude,
      },
    );
    return SittingPostResult.fromJson(response.data!);
  }

  Future<List<SittingPostResult>> listPosts() async {
    final response = await _dio.get<List<dynamic>>('/api/sitting-posts');
    return response.data!
        .cast<Map<String, dynamic>>()
        .map(SittingPostResult.fromJson)
        .toList();
  }

  Future<List<SittingPostResult>> listNearby({
    required double latitude,
    required double longitude,
    required double radiusKm,
  }) async {
    final response = await _dio.get<List<dynamic>>(
      '/api/sitting-posts/nearby',
      queryParameters: {
        'latitude': latitude,
        'longitude': longitude,
        'radiusKm': radiusKm,
      },
    );
    return response.data!
        .cast<Map<String, dynamic>>()
        .map(SittingPostResult.fromJson)
        .toList();
  }

  Future<SittingPostResult> assignPost({
    required int postId,
    required int sitterId,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/api/sitting-posts/$postId/assign',
      data: {'sitterId': sitterId},
    );
    return SittingPostResult.fromJson(response.data!);
  }
}

final sittingPostApiProvider = Provider<SittingPostApi>(
  (ref) => SittingPostApi(ref.watch(dioProvider)),
);
