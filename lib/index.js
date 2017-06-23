import { NativeModules, NativeAppEventEmitter } from 'react-native'

// import ShareExtension from 'react-native-share-extension'
// const { type, value } = await NativeModules.ShareExtension.data()
// NativeModules.ShareExtension.close()

const dataSubscriptions = [];
NativeAppEventEmitter.addEventListener('shareData', (data) => {
    dataSubscriptions.forEach(callback => callback(data));
});

export default {
  data: () => NativeModules.ReactNativeShareExtension.data(),
  close: () => NativeModules.ReactNativeShareExtension.close(),
  onData: (cb) => dataSubscriptions.push(cb),
}
