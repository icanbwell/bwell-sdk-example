import { useClientKey } from '@/components/client-key'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { useRouter } from 'expo-router'
import { View, Button } from 'react-native'

export default function Settings() {
  const router = useRouter()
  const clientKey = useClientKey()

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        padding: 8,
      }}>
      <Button title="Clear Storage" onPress={() => {
        clientKey.setKey('')
        AsyncStorage.clear().then(() => {
          router.replace('/')
        })
      }} />
    </View>
  )
}
