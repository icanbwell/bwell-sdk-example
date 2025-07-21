import { View, Text } from 'react-native'
import { Stack } from "expo-router";
import { useClientKey } from '@/components/client-key'

export default function RootStack() {
  const clientKey = useClientKey();

  if (clientKey.loading) {
    return (
      <View style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}>
        <Text>Loading client key</Text>
      </View>
    )
  }

  return (
    <Stack>
      <Stack.Protected guard={!clientKey.hasKey}>
        <Stack.Screen name="index" options={{ title: 'Client Key' }} />
      </Stack.Protected>

      <Stack.Protected guard={clientKey.hasKey}>
        <Stack.Screen name="sdk" options={{ headerShown: false }} />
      </Stack.Protected>
    </Stack>

  )
}
