import React, { useEffect, useState } from 'react';
import { openSelectionScreen , listenToTimeoutEvent} from 'react-native-inno';
import {
  StyleSheet,
  Alert,
  SafeAreaView,
  TouchableOpacity,
  Text,
  Platform,
} from 'react-native';
import { showEkycUI } from 'react-native-inno';
import { NativeEventEmitter, NativeModules } from 'react-native';
import VerificationScreen from './Verification';

const { LivelinessDetectionBridge } = NativeModules;

export default function App({ initialProps }: { initialProps: any }) {
  const { referenceNumber } = initialProps || {};
  const [referenceID, setReferenceID] = useState<string | null>(null);
  const [showVerification, setShowVerification] = useState(!!referenceNumber);
  const [clicked, setClicked] = useState<boolean>(false);

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

  useEffect(() => {
    const unsubscribe = listenToTimeoutEvent((event) => {
      Alert.alert(
        'Session Timeout',
        event.timeoutMessage || 'Session has timed out'
      );
    });

    return () => unsubscribe(); // Cleanup on unmount
  }, []);


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
        await openSelectionScreen(referenceNumber);
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
  };

  if (showVerification || (referenceID && clicked)) {
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
});
