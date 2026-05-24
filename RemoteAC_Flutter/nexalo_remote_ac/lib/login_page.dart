import 'package:flutter/material.dart';
import 'package:amplify_flutter/amplify_flutter.dart';
import 'package:amplify_auth_cognito/amplify_auth_cognito.dart';
import 'package:amplify_authenticator/amplify_authenticator.dart';
import 'amplify_outputs.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

}

class _LoginPageState extends State<LoginPage> {

  bool _isAmplifyConfigured = false;

  @override
  void initState() {
    super.initState();
    _configureAmplify();
  }

  Future<void> _configureAmplify() async {
    try {
      // 1. Add the Cognito Auth Plugin
      await Amplify.addPlugin(AmplifyAuthCognito());

      // 2. Load the configuration json file
      final configString = amplifyOutputs; // Using the hardcoded string from amplify_outputs.dart
      
      // 3. Configure Amplify
      await Amplify.configure(configString);
      safePrint('Successfully configured');
      setState(() {
        _isAmplifyConfigured = true;
      });
    } on Exception catch (e) {
      safePrint('Error configuring Amplify: $e');
    }
  }
  @override
  @override
  Widget build(BuildContext context) {
    if (!_isAmplifyConfigured) {
      return MaterialApp(
        title: 'Remote AC by Nexalo',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color.fromARGB(255, 23, 161, 112)),
        ),
        home: Scaffold(body: Center(child: CircularProgressIndicator())),
      );
    }

    return Authenticator(
      signUpForm: SignUpForm.custom(
        fields: [
          SignUpFormField.username(),
          SignUpFormField.email(required: true),
          SignUpFormField.password(),
          SignUpFormField.passwordConfirmation(),
        ],
      ),
      child: MaterialApp(
        builder: Authenticator.builder(),
        home: const MyHomePage(title: 'RemoteAC'),
      ),
    );
  }
}