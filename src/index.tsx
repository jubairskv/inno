import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-inno' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const LINKING_ERROR1 =
  `The package 'react-native-inno' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// if(Platform.OS === 'android'){
const SelectionActivity = NativeModules.SelectionActivity
  ? NativeModules.SelectionActivity
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
// }

const Inno = NativeModules.Inno
  ? NativeModules.Inno
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR1);
        },
      }
    );
if (Platform.OS === 'ios') {
  const innoEmitter = new NativeEventEmitter(Inno); // ✅ Add Event Emitter
}

// ✅ Show EKYC UI (Existing)
export function showEkycUI(): Promise<void> {
  return Inno.showEkycUI();
}

// ✅ Start Liveliness Detection & Receive `referenceID`
export function startLivelinessDetection(
  callback: (referenceID: string) => void
) {
  const subscription = innoEmitter.addListener(
    'onReferenceIDReceived',
    (referenceID: string) => {
      console.log('✅ Received Reference ID from iOS:', referenceID);
      callback(referenceID);
    }
  );

  Inno.startLivelinessDetection();

  return () => {
    subscription.remove(); // Cleanup listener when not needed
  };
}

export function openSelectionScreen(): Promise<boolean> {
  return SelectionActivity.openSelectionUI();
}
