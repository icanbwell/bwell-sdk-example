import { useClientKey } from '@/components/client-key'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { useRouter } from 'expo-router'
import { View, Text, Button } from 'react-native'

export default function Settings() {
  const router = useRouter()
  const clientKey = useClientKey()

  return (
    <View>
      <Text>Settings</Text>
      <Button title="Clear Storage" onPress={() => {
        clientKey.setKey('')
        AsyncStorage.clear().then(() => {
          router.replace('/')
        })
      }} />
    </View>
  )
}
