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
  const { auth } = useBWellSDK()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
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
        placeholder='email'
        textContentType='emailAddress'
        value={email}
        onChange={(e) => {
          setEmail(e.nativeEvent.text)
        }}
      />

      <TextInput
        style={styles.input}
        textContentType='password'
        placeholder='password'
        value={password}
        onChange={(e) => {
          setPassword(e.nativeEvent.text)
        }}
      />

      <Button title="Log In" onPress={() => {
        setLoading(true)
        auth(email, password)
      }} />

      {loading && (
        <Text>Logging In</Text>
      )}
    </View>
  )
}
