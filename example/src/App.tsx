import React, { useEffect, useState } from 'react';
import { 
  openSelectionScreen, showEkycUI
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

const { LivelinessDetectionBridge } = NativeModules;

export default function App({ initialProps }: { initialProps: any }) {
  const { referenceNumber, sessionTimeoutStatus } = initialProps || {};
  console.log(sessionTimeoutStatus,"session")
  console.log(referenceNumber,"referenceNumber")
  const [referenceID, setReferenceID] = useState<string | null>(null);
  const [showVerification, setShowVerification] = useState(!!referenceNumber);
  const [clicked, setClicked] = useState<boolean>(false);
  const [sessionTimeout, setSessionTimeout] = useState<boolean>(Boolean(sessionTimeoutStatus));
  console.log(sessionTimeout,"SessionTimeout")

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
      setClicked(true);
      try {
        await showEkycUI();
      } catch (error) {
        Alert.alert('Error', 'Failed to launch eKYC');
      }
    }
    if (Platform.OS === 'android') {
      setShowVerification(true);
      try {
        const referenceNumber = generateReferenceNumber(); // Call the function directly
        await openSelectionScreen(referenceNumber, "App");
        console.log('Selection screen opened');
      } catch (error) {
        console.error(error);
        Alert.alert('Error', 'Failed to open selection screen');
      }
    }
  };

  if (Platform.OS === 'ios') {
    useEffect(() => {
      const eventEmitter = new NativeEventEmitter(LivelinessDetectionBridge);

      const subscription = eventEmitter.addListener(
        'onReferenceIDReceived',
        (event) => {
          console.log(
            '✅ Reference ID received from native:',
            event.referenceID
          );
          setReferenceID(event.referenceID);
        }
      );

      return () => {
        subscription.remove();
      };
    }, []);
  }

  const handleCloseVerification = () => {
    setShowVerification(false);
    setClicked(false);
    setReferenceID(null);
    setSessionTimeout(false); // Reset session timeout state
  };

  const handleCloseSessionTimeout = () => {
    setShowVerification(false);
    setSessionTimeout(false);
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

  if (showVerification || sessionTimeout === 0 ||  (referenceID && clicked  ) ) {
    console.log("Navigation to verification")
    return Platform.OS === 'ios' ? (
      <VerificationScreen
        initialProps={{ referenceID }}
        onClose={handleCloseVerification}
      />
    ) : (
      <VerificationScreen
        initialProps={{ referenceNumber: referenceNumber || referenceID }}
        onClose={handleCloseVerification}
      />
    );
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