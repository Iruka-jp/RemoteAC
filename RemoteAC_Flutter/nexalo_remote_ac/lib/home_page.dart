import 'package:flutter/material.dart';
import 'package:wifi_scan/wifi_scan.dart';
import 'package:permission_handler/permission_handler.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final List<String> _addedACs = [];

  void _addRemote() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const AddRemotePage(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              _addedACs.isEmpty ? 'No ACs added yet.' : 'Your ACs:',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 20),
            ..._addedACs.map((ac) => Text(ac)),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addRemote,
        tooltip: 'Add AC',
        child: const Icon(Icons.add),
      ),
    );
  }
}

class AddRemotePage extends StatefulWidget {
  const AddRemotePage({super.key});

  @override
  State<AddRemotePage> createState() => _AddRemotePageState();
}

class _AddRemotePageState extends State<AddRemotePage> {
  final List<String> _logos = [
    'assets/logos/corona.png',
    'assets/logos/daikin.png',
    'assets/logos/hitachi.png',
    'assets/logos/mitsubishi_electric.png',
    'assets/logos/mitsubishi_heavy_industries.png',
    'assets/logos/panasonic.png',
    'assets/logos/toshiba.png',
  ];

  String? _selectedBrand;

  Future<void> _scanForRemote() async {
    // 1. Check/Request permissions
    
    // For Android 13+ (SDK 33), we need nearbyWifiDevices
    // For others and iOS, we need location
    Map<Permission, PermissionStatus> statuses = await [
      Permission.locationWhenInUse,
      if (Theme.of(context).platform == TargetPlatform.android) 
        Permission.nearbyWifiDevices,
    ].request();

    final locationStatus = statuses[Permission.locationWhenInUse];
    final nearbyStatus = statuses[Permission.nearbyWifiDevices];

    if (locationStatus?.isPermanentlyDenied == true || nearbyStatus?.isPermanentlyDenied == true) {
      if (mounted) {
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Permission Required'),
            content: const Text('Required permissions are permanently denied. Please enable Location and Nearby Devices in settings.'),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Permissions required to scan for WiFi.')),
                  );
                },
                child: const Text('Cancel'),
              ),
              TextButton(
                onPressed: () {
                  openAppSettings();
                  Navigator.pop(context);
                },
                child: const Text('Open Settings'),
              ),
            ],
          ),
        );
      }
      return;
    }

    if (locationStatus?.isGranted != true && (Theme.of(context).platform != TargetPlatform.android || nearbyStatus?.isGranted != true)) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Permissions are required to scan for WiFi.')),
        );
      }
      return;
    }

    // Check if Location Service is enabled (Required for WiFi scanning on many devices)
    if (!await Permission.locationWhenInUse.serviceStatus.isEnabled) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Please enable Location Services (GPS) on your device.')),
        );
      }
      return;
    }

    // 2. Start scan
    final canScan = await WiFiScan.instance.canStartScan();
    if (canScan != CanStartScan.yes) {
      String message = 'Cannot start scan: $canScan';
      if (canScan == CanStartScan.notSupported && Theme.of(context).platform == TargetPlatform.iOS) {
        message = 'iOS does not support full WiFi scanning. Please connect to the RemoteAC network in your system Settings.';
      } else if (canScan == CanStartScan.noLocationServiceDisabled) {
        message = 'Please enable Location Services (GPS) to scan.';
      }
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(message), duration: const Duration(seconds: 5)),
        );
      }
      return;
    }

    final result = await WiFiScan.instance.startScan();
    if (!result) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to start WiFi scan.')),
        );
      }
      return;
    }

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Scanning for RemoteAC...')),
      );
    }

    // 3. Get results (Wait a bit for scan to complete)
    await Future.delayed(const Duration(seconds: 2));
    final accessPoints = await WiFiScan.instance.getScannedResults();
    final remoteACs = accessPoints.where((ap) => ap.ssid.contains('RemoteAC_')).toList();

    if (mounted) {
      if (remoteACs.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No RemoteAC devices found.')),
        );
      } else {
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Found Devices'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: remoteACs.map((ap) => ListTile(
                title: Text(ap.ssid),
                subtitle: Text('Signal: ${ap.level} dBm'),
                onTap: () {
                  Navigator.of(context).pop();
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Connected to ${ap.ssid} (Mock)')),
                  );
                },
              )).toList(),
            ),
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Select AC Brand'),
      ),
      body: Column(
        children: [
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: _logos.length,
              itemBuilder: (context, index) {
                final logo = _logos[index];
                final brandName = logo.split('/').last.split('.').first.replaceAll('_', ' ');
                final isSelected = _selectedBrand == logo;

                return GestureDetector(
                  onTap: () {
                    setState(() {
                      _selectedBrand = logo;
                    });
                  },
                  child: Container(
                    decoration: BoxDecoration(
                      border: Border.all(
                        color: isSelected ? Colors.green : Colors.grey.shade300,
                        width: 2,
                      ),
                      borderRadius: BorderRadius.circular(8),
                      color: isSelected ? Colors.green.withOpacity(0.1) : null,
                    ),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: Image.asset(logo, height: 80, fit: BoxFit.contain),
                        ),
                        Text(
                          brandName.toUpperCase(),
                          style: TextStyle(
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(24.0),
            child: SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                onPressed: _selectedBrand != null ? _scanForRemote : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color.fromARGB(255, 23, 161, 112),
                  foregroundColor: Colors.white,
                ),
                child: const Text('Connect to Remote'),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
