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

const innoEmitter =
  Platform.OS === 'ios' && Inno ? new NativeEventEmitter(Inno) : null;

// Add TimeoutEventModule interface
interface TimeoutEventModule {
  addListener(eventType: string): void;
  removeListeners(count: number): void;
}

// Add timeout event interface
export interface TimeoutEvent {
  timeoutStatus: number;
  timeoutMessage: string | null;
}

const { TimeoutEventModule } = NativeModules;

// Create timeout event emitter
export const timeoutEmitter = Platform.OS === 'android' 
  ? new NativeEventEmitter(TimeoutEventModule) 
  : null;

// Add timeout event constants
export const SESSION_TIMEOUT_EVENT = 'onTimeoutEvent';

// Add timeout listener function
export function addTimeoutListener(
  callback: (event: TimeoutEvent) => void
): () => void {
  if (Platform.OS !== 'android' || !timeoutEmitter) {
    return () => {};
  }

  const subscription = timeoutEmitter.addListener(SESSION_TIMEOUT_EVENT, callback);
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
  if (!innoEmitter) {
    throw new Error('NativeEventEmitter not initialized');
  }

  const subscription = innoEmitter.addListener(
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
