import { useClientKey } from '@/components/client-key';
import { AuthTokens, BWellSDK, TokenStorage } from '@icanbwell/bwell-sdk-ts'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { createContext, PropsWithChildren, useContext, useEffect, useRef, useState } from 'react';

export const BWellSDKContext = createContext<BWellSDKContextValue | undefined>(undefined)

class AppStorage implements TokenStorage {
  prefix: string;

  constructor(prefix: string) {
    this.prefix = prefix;
  }

  async get(key: string): Promise<string | undefined> {
    const result = await AsyncStorage.getItem(this.keyPrefix(key))

    if (result === null) {
      return undefined
    }

    return result;
  }

  set(key: string, value: string): Promise<void> {
    return AsyncStorage.setItem(this.keyPrefix(key), value)
  }

  async delete(key: string): Promise<boolean> {
    await AsyncStorage.removeItem(this.keyPrefix(key))

    return true;
  }

  async loadAuth(): Promise<AuthTokens | null> {
    const [idTokenKv, accessTokenKv, refreshTokenKv] = await AsyncStorage.multiGet([
      this.keyPrefix('idToken'),
      this.keyPrefix('accessToken'),
      this.keyPrefix('refreshToken'),
    ])

    const [, idToken] = idTokenKv;
    const [, accessToken] = accessTokenKv;
    const [, refreshToken] = refreshTokenKv;

    if (idToken === null || accessToken === null || refreshToken === null) {
      return null;
    }

    return {
      idToken,
      accessToken,
      refreshToken,
    }
  }

  public keyPrefix(key: string): string {
    return `${this.prefix}:${key}`
  }
}

const storage = new AppStorage('MY_APP')

export type AsyncBoolState = {
  loading: boolean;
  state: boolean;
  error: Error | null;
}

export type BWellSDKContextValue = {
  initState: AsyncBoolState,
  authState: AsyncBoolState,
  // initialize: BWellSDK['initialize']
  // isAuthed: boolean,
  auth: (email: string, password: string) => void
  jwtAuth: (jwt: string) => void
  sdk: BWellSDK,
}

export function useInitSDK(): BWellSDKContextValue {
  const sdkRef = useRef<BWellSDK | undefined>(undefined)
  const clientKey = useClientKey();

  const [initState, setInitState] = useState<AsyncBoolState>({
    loading: false,
    state: false,
    error: null
  })

  const [authState, setAuthState] = useState<AsyncBoolState>({
    loading: false,
    state: false,
    error: null
  })

  useEffect(() => {
    if (!clientKey.hasKey) {
      return
    }

    let sdk: BWellSDK;

    try {
      sdk = new BWellSDK({
        clientKey: clientKey.key,
        tokenStorage: storage,
      });

      sdkRef.current = sdk;
    } catch (e) {
      const err = e as Error;
      console.log(err);

      clientKey.setError(err.message);
      clientKey.setKey("");

      setInitState({
        loading: false,
        state: false,
        error: err,
      });

      return;
    }

    const init = async () => {
      console.debug('initializing sdk')
      setInitState({ loading: true, state: false, error: null })
      const initResult = await sdk.initialize();

      if (initResult.failure()) {
        console.warn('failed to initialize sdk')
        setInitState({ loading: false, state: false, error: initResult.error() })
        return
      }

      console.info('successfully initialized sdk')
      setInitState({ loading: false, state: true, error: null })

      const credentials = await storage.loadAuth();

      if (credentials === null) {
        console.debug('could not load credentials. will not auth')
        return
      }

      console.debug('found credentials. attempting to log in...')
      setAuthState({ state: false, loading: true, error: null });
      const authResult = await sdk.setAuthTokens(credentials);

      if (authResult.failure()) {
        setAuthState({ state: false, loading: false, error: authResult.error() });
        console.log('failed to auth with loaded credentials', authResult.error())
        return
      }

      setAuthState({ state: true, loading: false, error: null })
    }

    init();
  }, [clientKey.key, clientKey.hasKey])

  const auth = async (email: string, password: string) => {
    const sdk = sdkRef.current;

    if (sdk === undefined) {
      throw new Error('SDK Uninitialized')
    }

    setAuthState({ loading: true, state: false, error: null })

    const result = await sdk.authenticate({
      email,
      password,
    })

    if (result.failure()) {
      setAuthState({ state: false, loading: false, error: result.error() })
      return;
    }

    setAuthState({ state: true, loading: false, error: null })
  }

  const jwtAuth = async (jwt: string) => {
    const sdk = sdkRef.current;

    if (sdk === undefined) {
      throw new Error('SDK Uninitialized')
    }

    setAuthState({ loading: true, state: false, error: null })

    const result = await sdk.authenticate({
      token: jwt,
    });

    if (result.failure()) {
      setAuthState({ state: false, loading: false, error: result.error() })
      return;
    }

    setAuthState({ state: true, loading: false, error: null })
  }

  return {
    initState,
    authState,
    auth,
    jwtAuth,
    sdk: sdkRef.current!,
  }
}

export function BWellSDKProvider(props: PropsWithChildren) {
  const sdk = useInitSDK()

  if (sdk.sdk === undefined) {
    return null
  }

  return (
    <BWellSDKContext.Provider value={sdk}>
      {props.children}
    </BWellSDKContext.Provider>
  )
}

export function useBWellSDK(): Required<BWellSDKContextValue> {
  const sdk = useContext(BWellSDKContext)

  if (sdk === undefined) {
    throw new Error('No bwell sdk!')
  }

  return sdk;
}
