import { useBWellSDK } from '@/components/sdk'
import { useState } from 'react'
import { View, Text, TextInput, Button, StyleSheet } from 'react-native'

const styles = StyleSheet.create({
  input: {
    padding: 4,
    borderWidth: 1,
    margin: 12,
    width: '100%',
  }
})

export default function LoginScreen() {
  const { jwtAuth, authState } = useBWellSDK()
  const [jwt, setJwt] = useState('')
  const [loading, setLoading] = useState(false)

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        padding: 8,
      }}
    >
      <TextInput
        style={styles.input}
        placeholder='Token'
        textContentType='emailAddress'
        value={jwt}
        onChange={(e) => {
          setJwt(e.nativeEvent.text)
        }}
      />

      <Button
        title="Log In"
        onPress={() => {
          setLoading(true)
          jwtAuth(jwt);
        }}
      />

      {authState.error !== null && (
        <Text style={{ color: 'red' }}>
          {authState.error.message}
        </Text>
      )}

      {loading && (
        <Text>Logging In</Text>
      )}
    </View>
  )
}
