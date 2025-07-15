import AsyncStorage from '@react-native-async-storage/async-storage'
import { createContext, PropsWithChildren, useContext, useEffect, useState } from 'react'

export const ClientKeyContext = createContext<ClientKeyValue | null>(null)
const CLIENT_KEY_STORAGE_KEY = 'bwell-client-key'

type ClientKeyState = {
  key: string,
  hasKey: boolean,
  loading: boolean,
  error: string | null,
}

type ClientKeyValue = ClientKeyState & {
  setKey: (key: string) => Promise<void>
  setError: (err: string | null) => void
}

export function ClientKeyProvider({ children }: PropsWithChildren) {
  const [keyState, setKeyState] = useState<ClientKeyState>({
    key: '',
    hasKey: false,
    loading: false,
    error: null,
  });

  useEffect(() => {
    setKeyState((prev) => {
      return {
        ...prev,
        loading: true,
      }
    })


    console.debug('getting client key from storage')

    AsyncStorage.getItem(CLIENT_KEY_STORAGE_KEY).then(key => {
      let hasKey = false;
      let stateKey = '';

      console.info('storage key', key)

      if (key !== null) {
        stateKey = key;
        hasKey = true
      }

      setKeyState({
        error: null,
        key: stateKey,
        hasKey,
        loading: false,
      })
    })
  }, [])

  const setError = (err: string | null) => {
    setKeyState((state) => {
      return {
        ...state,
        error: err,
      }
    })
  }

  const setKey = async (key: string) => {
    try {
      console.log('setting key')

      await AsyncStorage.setItem(CLIENT_KEY_STORAGE_KEY, key)

      setKeyState((state) => ({
        ...state,
        key,
        hasKey: key.length > 0,
        loading: false,
      }))
    } catch (e) {
      console.error('Error setting client key storage', e)
    }
  }

  return (
    <ClientKeyContext.Provider value={{ ...keyState, setKey, setError }}>
      {children}
    </ClientKeyContext.Provider>
  )
}

export function useClientKey(): ClientKeyValue {
  const value = useContext(ClientKeyContext)

  if (value === null) {
    throw new Error('client key context absent')
  }

  return value;
}
