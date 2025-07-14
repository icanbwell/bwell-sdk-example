import { useBWellSDK } from '@/components/sdk'
import { useState } from 'react'
import { View, Text, TextInput, Button } from 'react-native'

export default function LoginScreen() {
  const { auth } = useBWellSDK()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  return (
    <View>
      <TextInput
        placeholder='email'
        textContentType='emailAddress'
        value={email}
        onChange={(e) => {
          setEmail(e.nativeEvent.text)
        }}
      />

      <TextInput
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
