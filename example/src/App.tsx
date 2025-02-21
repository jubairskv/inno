import React, { useEffect, useState } from 'react';
import { openSelectionScreen } from 'react-native-inno';
import {
  View,
  StyleSheet,
  Alert,
  SafeAreaView,
  TouchableOpacity,
  Text,
} from 'react-native';
import {
  showEkycUI,
  innoEmitter,
  VERIFICATION_COMPLETE_EVENT,
} from 'react-native-inno';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import VerificationScreen from './Verification';

const { LivelinessDetectionBridge } = NativeModules;

export default function App({ initialProps }) {
  const { referenceNumber } = initialProps || {};
  const [referenceID, setReferenceID] = useState<string | null>(null);
  const [showVerification, setShowVerification] = useState(!!referenceNumber);
  const [clicked, setClicked] = useState<boolean>(false);

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
      setClicked(true);
      try {
        await openSelectionScreen();
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
