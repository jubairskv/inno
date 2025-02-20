import { openSelectionScreen } from 'react-native-inno';
import {
  StyleSheet,
  Alert,
  SafeAreaView,
  TouchableOpacity,
  Text,
} from 'react-native';
// import { showEkycUI, camOcrLibEmitter, VERIFICATION_COMPLETE_EVENT } from 'react-native-cam-ocr-lib';
import { showEkycUI } from 'react-native-inno';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import VerificationScreen from './Verificationn';

const { LivelinessDetectionBridge } = NativeModules;

export default function App() {
  const [referenceID, setReferenceID] = useState<string | null>(null);
  const [clicked, setClicked] = useState<boolean>(false);

  const startEkyc = async () => {
    //
    if (Platform.OS === 'ios') {
      setClicked(true);
      try {
        await showEkycUI();
      } catch (error) {
        Alert.alert('Error', 'Failed to launch eKYC');
      }
    }
    if (Platform.OS === 'android') {
      try {
        await openSelectionScreen();
        console.log('Selection screen opened');
      } catch (error) {
        console.error(error);
        Alert.alert('Error', 'Failed to open selection screen');
      }
    }
  };

  // if (Platform.OS === 'ios') {
  //   useEffect(() => {
  //     const eventEmitter = new NativeEventEmitter(LivelinessDetectionBridge);

  //     // ✅ Listen for the event
  //     const subscription = eventEmitter.addListener(
  //       'onReferenceIDReceived',
  //       (event) => {
  //         console.log(
  //           '✅ Reference ID received from native:',
  //           event.referenceID
  //         );
  //         setReferenceID(event.referenceID);
  //       }
  //     );

  //     return () => {
  //       subscription.remove();
  //     };
  //   }, []);
  // }

  useEffect(() => {
    if (Platform.OS === 'ios') {
      const eventEmitter = new NativeEventEmitter(LivelinessDetectionBridge);

      // ✅ Listen for the event
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
    }
  }, []); // ✅ `useEffect` is always called

  const handleCloseVerification = () => {
    setClicked(false);
    setReferenceID(null);
  };

  // return (
  //   <SafeAreaView style={styles.container}>
  //     <TouchableOpacity style={styles.button} onPress={handleOpenSelection}>
  //       <Text style={styles.buttonText}>Launch eKYC</Text>
  //     </TouchableOpacity>
  //   </SafeAreaView>
  // );

  if (!referenceID && !clicked) {
    return (
      <SafeAreaView style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={startEkyc}>
          <Text style={styles.buttonText}>Launch eKYC</Text>
        </TouchableOpacity>
      </SafeAreaView>
    );
  }
  if (referenceID && clicked) {
    console.log(referenceID, '-------------++++++--------------');
    return (
      <VerificationScreen
        referenceID={{ referenceID }}
        onClose={handleCloseVerification}
      />
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  button: {
    backgroundColor: '#007BFF', // Custom background color
    padding: 15,
    margin: 20,
    borderRadius: 5,
    alignItems: 'center',
  },
  buttonText: {
    color: '#FFFFFF', // Custom text color
    fontSize: 16,
    fontWeight: 'bold',
  },
  },
});

// import { openSelectionScreen } from 'react-native-inno';
// import {
//   StyleSheet,
//   Alert,
//   SafeAreaView,
//   TouchableOpacity,
//   Text,
// } from 'react-native';

// export default function App() {
//   const handleOpenSelection = async () => {
//     if(Platform.OS === 'android'){
//     try {
//       await openSelectionScreen();
//       console.log('Selection screen opened');
//     } catch (error) {
//       console.error(error);
//       Alert.alert('Error', 'Failed to open selection screen');
//     }
//   }else{
//     useEffect(() => {
//             const eventEmitter = new NativeEventEmitter(LivelinessDetectionBridge);

//             // ✅ Listen for the event
//             const subscription = eventEmitter.addListener("onReferenceIDReceived", (event) => {
//               console.log("✅ Reference ID received from native:", event.referenceID);
//               setReferenceID(event.referenceID);
//             });

//             return () => {
//               subscription.remove();
//             };
//           }, []);
//   // }
//   };
// }

//   return (
//     <SafeAreaView style={styles.container}>
//       <TouchableOpacity style={styles.button} onPress={handleOpenSelection}>
//         <Text style={styles.buttonText}>Launch eKYC</Text>
//       </TouchableOpacity>
//     </SafeAreaView>
//   );
// }

// const styles = StyleSheet.create({
//   container: {
//     flex: 1,
//     justifyContent: 'flex-end',
//   },
//   button: {
//     backgroundColor: '#007BFF', // Custom background color
//     padding: 15,
//     margin: 20,
//     borderRadius: 5,
//     alignItems: 'center',
//   },
//   buttonText: {
//     color: '#FFFFFF', // Custom text color
//     fontSize: 16,
//     fontWeight: 'bold',
//   },
// });

// import React, { useEffect } from 'react';
// import { NavigationContainer } from '@react-navigation/native';
// import { createStackNavigator } from '@react-navigation/stack';
// import { View, Text } from 'react-native';
// import Verification from './Verification'; // Import your Verification screen
// import Home from './Home'; // Your main home screen

// const Stack = createStackNavigator();

// const MainNavigator = ({ initialProps }) => {
//   useEffect(() => {
//     console.log('Initial Props from Android:', initialProps);
//   }, []);

//   return (
//     <NavigationContainer>
//       <Stack.Navigator>
//         <Stack.Screen name="Home" component={Home} />
//         <Stack.Screen
//           name="Verification"
//           component={Verification}
//           initialParams={initialProps} // Pass initial props to Verification screen
//         />
//       </Stack.Navigator>
//     </NavigationContainer>
//   );
// };
