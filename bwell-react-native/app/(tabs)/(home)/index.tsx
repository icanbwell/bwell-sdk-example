import { useClientKey } from '@/components/client-key';
import { useState } from "react";
import { Text, View, TextInput, StyleSheet, Button } from "react-native";

const styles = StyleSheet.create({
  input: {
    height: 40,
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
      }}
    >
      <Text>Provide a dev client key plz</Text>
      <TextInput value={token} onChange={(e) => setToken(e.nativeEvent.text)} style={styles.input} />
      <Button title="Go" onPress={() => {
        clientKey.setKey(token)
      }} />
    </View>
  );
}
