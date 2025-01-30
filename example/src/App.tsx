import React from 'react';
import { getDummyText, toggleCamera } from 'react-native-inno';
import { View, StyleSheet, Button, Alert } from 'react-native';

function getReferenceId(): string {
  const currentDate = new Date();

  // Get current date and time components
  const day = String(currentDate.getDate()).padStart(2, '0');
  const month = String(currentDate.getMonth() + 1).padStart(2, '0');
  const year = currentDate.getFullYear();
  const hours = String(currentDate.getHours()).padStart(2, '0');
  const minutes = String(currentDate.getMinutes()).padStart(2, '0');
  const seconds = String(currentDate.getSeconds()).padStart(2, '0');

  // Format in ddmmyyyyhhmmss
  const formattedDateTime = `${day}${month}${year}${hours}${minutes}${seconds}`;

  // Generate a random number
  const randomNumber = Math.floor(Math.random() * 1000); // Random number between 0 and 999

  // Concatenate the formatted date-time string with the random number
  let referenceId = `${formattedDateTime}${randomNumber}`;

  // Ensure the length does not exceed 32 characters
  if (referenceId.length > 32) {
    referenceId = referenceId.substring(0, 32); // Trim to 32 characters
  }

  return referenceId;
}

export default function App() {
  const handleToggleCamera = async () => {
    try {
      const referenceId = getReferenceId(); // Get the formatted reference ID
      await getDummyText('INNOVERIFY' + referenceId);
      await toggleCamera();
    } catch (error) {
      console.error(error);
      Alert.alert('Error', 'Failed to toggle camera');
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Toggle Camera" onPress={handleToggleCamera} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
