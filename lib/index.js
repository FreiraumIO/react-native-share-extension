import { NativeModules } from 'react-native'
export default {
  data: () => NativeModules.ReactNativeShareExtension.data(),
  close: () => NativeModules.ReactNativeShareExtension.close(),
  clear: () => NativeModules.ReactNativeShareExtension.clear(),
}
