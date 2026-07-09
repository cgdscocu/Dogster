import 'package:dogster_mobile/app.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  testWidgets('Dogster app opens auth flow', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: DogsterApp()));

    expect(find.text('Dogster'), findsOneWidget);
    expect(find.text('Register'), findsOneWidget);
    await tester.drag(find.byType(Scrollable).first, const Offset(0, -500));
    await tester.pump();
    expect(find.text('Verify Email'), findsOneWidget);
  });
}
