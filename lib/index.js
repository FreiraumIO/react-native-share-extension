import { NativeModules } from "react-native";
export default {
  data: (appGroupId) => NativeModules.ReactNativeShareExtension.data(appGroupId),
  dataMulti: (appGroupId) => NativeModules.ReactNativeShareExtension.dataMulti(appGroupId),
  close: (appGroupId) => NativeModules.ReactNativeShareExtension.close(appGroupId),
  openURL: url => NativeModules.ReactNativeShareExtension.openURL(url)
};
