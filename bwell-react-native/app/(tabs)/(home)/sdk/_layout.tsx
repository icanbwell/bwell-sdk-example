import { Stack } from 'expo-router'
import { View, Text, StyleSheet } from 'react-native'
import { BWellSDKProvider, useBWellSDK } from '@/components/sdk'

const styles = StyleSheet.create({
  view: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  }
})

function HomeStack() {
  const { authState, initState } = useBWellSDK()

  if (authState.loading) {
    return (
      <View style={styles.view}>
        <Text>Authenticating...</Text>
      </View>
    )
  }

  if (initState.loading) {
    return (
      <View style={styles.view}>
        <Text>Initializing...</Text>
      </View>
    )
  }

  return (
    <Stack>
      <Stack.Protected guard={authState.state}>
        <Stack.Screen name="index" options={{ title: 'Health Summary' }} />
      </Stack.Protected>

      <Stack.Protected guard={!authState.state}>
        <Stack.Screen name="login" options={{ title: "Log In" }} />
      </Stack.Protected>
    </Stack>
  )
}

export default function HomeLayout() {
  return (
    <BWellSDKProvider>
      <HomeStack />
    </BWellSDKProvider>
  )
}
