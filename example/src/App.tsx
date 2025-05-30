import React, { useEffect, useState } from 'react';
import {
  showEkycUI
} from '@innovitegranpm/innotrust-rn-eth';
import {
  StyleSheet,
  Alert,
  SafeAreaView,
  TouchableOpacity,
  Text,
  Platform,
  View,
} from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';
import VerificationScreen from './Verification';


const { LivelinessDetectionBridge, SelectionModule } = NativeModules;
const Inno = NativeModules.Inno;
const innoEmitter = Platform.OS === 'ios' && Inno ? new NativeEventEmitter(Inno) : null;

export default function App() {
  // const { referenceNumber, sessionTimeoutStatus } = initialProps || {};
  const [timeoutStatus, setTimeoutStatus] = useState<string | null>("");
  const [referenceID, setReferenceID] = useState<string | null>(null);
  // const [showVerification, setShowVerification] = useState(!!referenceNumber);
  const [clicked, setClicked] = useState<boolean>(false);
  const [sessionTimeout, setSessionTimeout] = useState<boolean>(false);
  // 
  const [success, setSuccess] = useState<number>(-1);

  const generateReferenceNumber = () => {
    try {
      const currentDate = new Date();
      const dateFormatter = new Intl.DateTimeFormat('en-US', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      }).format(currentDate);
      const formattedDateTime = dateFormatter.replace(/[^0-9]/g, '');
      const randomNumber = Math.floor(Math.random() * 1000)
        .toString()
        .padStart(3, '0');
      let referenceId = `INNOVERIFYJUB${formattedDateTime}${randomNumber}`;

      if (referenceId.length > 32) {
        referenceId = referenceId.substring(0, 32);
      }

      console.log('Generated reference number:', referenceId);
      return referenceId;
    } catch (error) {
      console.error('Failed to generate reference number:', error.message);
      return `INNOVERIFYJUB${Date.now()}`; // Fallback reference number
    }
  };

  const startEkyc = async () => {
    if (Platform.OS === 'ios') {
      const referenceNumber = generateReferenceNumber()
      console.log('Reference Number: inside App', referenceNumber);
      setReferenceID(referenceNumber)
      setClicked(true);
      try {
        await showEkycUI(referenceNumber);
      } catch (error) {
        Alert.alert('Error', 'Failed to launch eKYC');
      }
    }
    if (Platform.OS === 'android') {
      // setShowVerification(true);
      try {
        const referenceNumber = generateReferenceNumber(); // Call the function directly
        setReferenceID(referenceNumber)
        //const apkName = await DeviceInfo.getApplicationName();
        // await openSelectionScreen(referenceNumber);
        SelectionModule.openSelectionScreen(referenceNumber, (result, sessionTimeoutStatus) => {
          console.log("Native verification result:------------", result, sessionTimeoutStatus);
          setSuccess(result);
          // Handle the result accordingly while preserving your navigation and state.
        });
        console.log('Selection screen opened');
      } catch (error) {
        console.error(error);
        Alert.alert('Error', 'Failed to open selection screen');
      }
    }
  };

  useEffect(() => {
    console.log("success---------------------", success)

    return () => {
    }
  }, [success])


  if (Platform.OS === 'ios') {
    useEffect(() => {
      const eventEmitter = new NativeEventEmitter(LivelinessDetectionBridge);

      const subscription = eventEmitter.addListener(
        'verificationStatus',
        (event) => {
          console.log(
            'Received Verification status',
            event.verificationStatus
          );
          // setReferenceID(event.sessionTimeout);
          setTimeoutStatus(event.sessionTimeout);
        }
      );

      const subscriptionTimeout = innoEmitter.addListener(
        'onScreenTimeout',
        (value) => {
          console.log('Screen timed out with value:', value);
          // Handle timeout event here (e.g., reset state or navigate)
          setClicked(false);
          setReferenceID(null);

          Alert.alert('Timeout', 'The native screen was closed due to inactivity.');
        }
      );

      return () => {
        subscription.remove();
        subscriptionTimeout.remove();
      };
    }, []);
  }
  const handleCloseVerification = () => {
    // setShowVerification(false);
    setClicked(false);
    setReferenceID(null);
    // setSessionTimeout(false); // Reset session timeout state
  };

  const handleCloseSessionTimeout = () => {
    // setShowVerification(false);
    // setSessionTimeout(false);
    setClicked(false);
    setReferenceID(null);
  };

  if (sessionTimeout) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>Session Timeout. Please try again.</Text>
          <TouchableOpacity style={styles.closeButton} onPress={handleCloseSessionTimeout}>
            <Text style={styles.closeButtonText}>Close</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }


  console.log('Reference Number: inside App----outside', referenceID, 'clicked', clicked, 'success', success);
  if (referenceID && success == 0) {
  //   console.log('Reference Number: inside App----inside', referenceID, 'clicked', clicked, 'success', success);
  //   return Platform.OS === 'ios' ? (
  //     <VerificationScreen
  //       initialProps={{ referenceNumber: referenceID }}
  //       onClose={handleCloseVerification}
  //     />
  //   ) : (
  //     <VerificationScreen
  //       initialProps={{ referenceNumber: referenceID }}
  //       onClose={handleCloseVerification}
  //     />
  //   );
    return (
      <VerificationScreen
        initialProps={{ referenceNumber: referenceID }}
        onClose={handleCloseVerification}
      />
    )
  }
  if (!referenceID && !clicked) {
    return (
      <SafeAreaView style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={startEkyc}>
          <Text style={styles.buttonText}>Launch eKYC</Text>
        </TouchableOpacity>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  button: {
    backgroundColor: '#007BFF',
    padding: 15,
    margin: 20,
    borderRadius: 5,
    alignItems: 'center',
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorText: {
    fontSize: 18,
    color: 'red',
    marginBottom: 20,
  },
  closeButton: {
    backgroundColor: '#007BFF',
    padding: 15,
    borderRadius: 5,
    alignItems: 'center',
    width: '90%', // Increase button width
    position: 'absolute', // Position at bottom
    bottom: 20, // Adjust as needed
    alignSelf: 'center', // Center horizontally
  },
  closeButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
});