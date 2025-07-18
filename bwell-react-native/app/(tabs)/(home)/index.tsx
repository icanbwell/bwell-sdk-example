import { useClientKey } from '@/components/client-key';
import { useState } from "react";
import { Text, View, TextInput, StyleSheet, Button } from "react-native";

const styles = StyleSheet.create({
  input: {
    width: '100%',
    margin: 12,
    borderWidth: 1,
    padding: 10,
  },
});


export default function Index() {
  const clientKey = useClientKey()
  const [token, setToken] = useState(clientKey.key);

  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        padding: 8,
      }}
    >
      <Text>Provide A Client Key</Text>
      <TextInput
        multiline
        numberOfLines={4}
        placeholder="Client Key"
        value={token}
        onChange={(e) => setToken(e.nativeEvent.text)}
        style={styles.input}
      />
      <Button
        title="Go"
        disabled={token.length === 0}
        onPress={() => {
          clientKey.setError(null);
          clientKey.setKey(token)
        }}
      />
      <Button
        title="Clear"
        disabled={token.length === 0}
        onPress={() => {
          setToken('')
        }}
      />
      {clientKey.error !== null && (
        <View><Text style={{ color: 'red' }}>{clientKey.error}</Text></View>
      )}
    </View>
  );
}
