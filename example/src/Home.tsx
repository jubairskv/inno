// import React from 'react';
// import { toggleCamera } from 'react-native-inno';
// import {
//   View,
//   StyleSheet,
//   Alert,
//   SafeAreaView,
//   TouchableOpacity,
//   Text,
// } from 'react-native';

// export default function App() {
//   const handleToggleCamera = async () => {
//     try {
//       await toggleCamera();
//     } catch (error) {
//       console.error(error);
//       Alert.alert('Error', 'Failed to toggle camera');
//     }
//   };

//   return (
//     <SafeAreaView style={styles.container}>
//       <TouchableOpacity style={styles.button} onPress={handleToggleCamera}>
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
