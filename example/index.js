import { AppRegistry } from 'react-native';
import App from './src/App';
import Verification from './src/Verification';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('Verification', () => Verification);
