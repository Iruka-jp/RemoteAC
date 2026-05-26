import 'package:flutter/material.dart';
import 'package:amplify_flutter/amplify_flutter.dart';
import 'package:amplify_auth_cognito/amplify_auth_cognito.dart';
import 'package:amplify_authenticator/amplify_authenticator.dart';
import 'amplify_outputs.dart';
import 'home_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _isAmplifyConfigured = false;

  @override
  void initState() {
    super.initState();
    _configureAmplify();
  }

  Future<void> _configureAmplify() async {
    final stopwatch = Stopwatch()..start();
    try {
      if (!Amplify.isConfigured) {
        await Amplify.addPlugin(AmplifyAuthCognito());
        await Amplify.configure(amplifyOutputs);
      }
      safePrint('Successfully configured');
    } on Exception catch (e) {
      safePrint('Error configuring Amplify: $e');
    } finally {
      stopwatch.stop();
      final elapsed = stopwatch.elapsedMilliseconds;
      if (elapsed < 2000) {
        await Future.delayed(Duration(milliseconds: 2000 - elapsed));
      }
      if (mounted) {
        setState(() {
          _isAmplifyConfigured = true;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_isAmplifyConfigured) {
      return MaterialApp(
        title: 'Remote AC by Nexalo',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color.fromARGB(255, 23, 161, 112)),
        ),
        home: Scaffold(
          body: Center(
            //child: Padding(
              //padding: const EdgeInsets.symmetric(horizontal: 40),
            child: Image.asset('assets/logos/logo_nexalo.png'),
            //),
          ),
        ),
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
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color.fromARGB(255, 23, 161, 112)),
        ),
        builder: Authenticator.builder(),
        home: const MyHomePage(title: 'RemoteAC'),
      ),
    );
  }
}
