import 'package:flutter/material.dart';
import 'login_page.dart';

void main() {
  runApp(const RemoteACApp());
}

class RemoteACApp extends StatefulWidget {
  const RemoteACApp({super.key});

  @override
  State<RemoteACApp> createState() => _RemoteACAppState();
}

class _RemoteACAppState extends State<RemoteACApp> {
  @override
  Widget build(BuildContext context) {
    // LoginPage now provides its own MaterialApp and Authenticator logic
    return const LoginPage();
  }
}
