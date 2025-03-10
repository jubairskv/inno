import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

const LINKING_ERROR =
  //The package 'react-native-inno' doesn't seem to be linked. Make sure: \n\n +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Platform-Specific Module Initialization
const SelectionActivity =
  Platform.OS === 'android'
    ? NativeModules.SelectionActivity ||
      new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      )
    : null;

const Inno =
  Platform.OS === 'ios'
    ? NativeModules.Inno ||
      new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      )
    : null;

const TimeoutEventModule = 
  Platform.OS === 'android'
    ? NativeModules.TimeoutEventModule ||
      new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      )
    : null;

// Create event emitters only if the corresponding modules exist
let iosEmitter: NativeEventEmitter | null = null;
let androidEmitter: NativeEventEmitter | null = null;

if (Platform.OS === 'ios' && Inno) {
  iosEmitter = new NativeEventEmitter(Inno);
}

if (Platform.OS === 'android' && TimeoutEventModule) {
  androidEmitter = new NativeEventEmitter(TimeoutEventModule);
}

export const TIMEOUT_EVENT = 'onTimeoutEvent';

export type TimeoutEventData = {
  timeoutStatus: number;
  timeoutMessage: string | null;
};

export function addTimeoutEventListener(
  callback: (event: TimeoutEventData) => void
) {
  if (Platform.OS !== 'android' || !androidEmitter) {
    console.warn('Timeout event listening is only available on Android');
    return () => {};
  }

  const subscription = androidEmitter.addListener(TIMEOUT_EVENT, callback);
  return () => subscription.remove();
}

// iOS-Specific Functions
export function showEkycUI(): Promise<void> {
  if (Platform.OS !== 'ios') {
    return Promise.reject('showEkycUI is only available on iOS');
  }
  return Inno.showEkycUI();
}

export function startLivelinessDetection(
  callback: (referenceID: string) => void
) {
  if (Platform.OS !== 'ios') {
    throw new Error('startLivelinessDetection is only available on iOS');
  }
  if (!iosEmitter) {
    throw new Error('NativeEventEmitter not initialized for iOS');
  }

  const subscription = iosEmitter.addListener(
    'onReferenceIDReceived',
    (referenceID: string) => {
      console.log('Received Reference ID:', referenceID);
      callback(referenceID);
    }
  );

  Inno.startLivelinessDetection();

  return () => subscription.remove();
}

// Android-Specific Function
export function openSelectionScreen(referenceNumber: string): Promise<boolean> {
  if (Platform.OS !== 'android') {
    return Promise.reject('openSelectionScreen is only available on Android');
  }
  return SelectionActivity.openSelectionUI(referenceNumber);
}

