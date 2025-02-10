import React from 'react';
// import { toggleCamera } from 'react-native-inno';
import { openSelectionScreen } from 'react-native-inno';
import {
  View,
  StyleSheet,
  Alert,
  SafeAreaView,
  TouchableOpacity,
  Text,
} from 'react-native';

export default function App() {
  const handleOpenSelection = async () => {
    try {
      await openSelectionScreen();
      console.log('Selection screen opened');
    } catch (error) {
      console.error(error);
      Alert.alert('Error', 'Failed to open selection screen');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <TouchableOpacity style={styles.button} onPress={handleOpenSelection}>
        <Text style={styles.buttonText}>Launch eKYC</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
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
});

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

// export default MainNavigator;
