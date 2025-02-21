import { AppRegistry } from 'react-native';
import App from './src/App';
//import Verification from './src/Verification';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
// AppRegistry.registerComponent('Verification', () => Verification);

// import { AppRegistry } from 'react-native';
// import { name as appName } from './app.json';
// import MainNavigator from './src/App';

// const initialProps = global.initialProps || {}; // Read props from Android

// const AppWithProps = () => <MainNavigator initialProps={initialProps} />;

// AppRegistry.registerComponent(appName, () => AppWithProps);
