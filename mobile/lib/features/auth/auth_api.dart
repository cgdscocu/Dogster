import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';

class UserResult {
  const UserResult({
    required this.userId,
    required this.email,
    required this.verified,
  });

  final int userId;
  final String email;
  final bool verified;

  factory UserResult.fromJson(Map<String, dynamic> json) {
    return UserResult(
      userId: json['userId'] as int,
      email: json['email'] as String,
      verified: json['verified'] as bool,
    );
  }
}

class AuthApi {
  AuthApi(this._dio);

  final Dio _dio;

  Future<UserResult> register({
    required String fullName,
    required String email,
    required String password,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/api/users/register',
      data: {'fullName': fullName, 'email': email, 'password': password},
    );
    return UserResult.fromJson(response.data!);
  }

  Future<UserResult> verifyEmail({
    required String email,
    required String code,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/api/users/verify-email',
      data: {'email': email, 'code': code},
    );
    return UserResult.fromJson(response.data!);
  }
}

final authApiProvider = Provider<AuthApi>(
  (ref) => AuthApi(ref.watch(dioProvider)),
);
