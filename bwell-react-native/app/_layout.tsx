import { Stack } from "expo-router";
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ClientKeyProvider } from '@/components/client-key'

const client = new QueryClient();

export default function RootLayout() {
  return (
    <QueryClientProvider client={client}>
      <ClientKeyProvider>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        </Stack>
      </ClientKeyProvider>
    </QueryClientProvider>
  )
}
