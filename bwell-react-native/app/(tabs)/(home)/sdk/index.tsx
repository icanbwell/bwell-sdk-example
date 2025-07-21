import { View, Text, FlatList, StyleSheet } from 'react-native'
import { Link } from 'expo-router'
import { useQuery } from '@tanstack/react-query'
import { useBWellSDK } from '@/components/sdk'
import { SafeAreaView } from 'react-native-safe-area-context'

export default function HomeScreen() {
  const { sdk } = useBWellSDK()

  const healthSummary = useQuery({
    queryKey: ['health-summary'],
    queryFn: () => {
      return sdk.health.getHealthSummary()
    }
  })

  if (healthSummary.isLoading) {
    return (
      <SafeAreaView
        style={{
          flex: 1,
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Text>Loading Health Summary</Text>
      </SafeAreaView>
    )
  }

  if (healthSummary.isError) {
    return (
      <SafeAreaView
        style={{
          flex: 1,
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Text>Error loading health summary</Text>
      </SafeAreaView>
    )
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={healthSummary.data?.data?.resources ?? []}
        renderItem={(item) => <ListItem category={item.item.category ?? ''} total={item.item.total ?? 0} />}
        keyExtractor={(item) => item.category as string}
      />
    </View>
  )
}

function ListItem({ category, total }: { category: string, total: number }) {
  return (
    <Link href={{ pathname: '/sdk/[type]', params: { type: category } }}>
      <View style={styles.item}>
        <Text style={styles.categoryText}>{category}</Text>
        <Text>{total}</Text>
      </View>
    </Link>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  item: {
    flexDirection: 'row',
    width: '100%',
    padding: 16,
    justifyContent: 'space-between',
    borderBottomWidth: 0.5,
    borderBottomColor: 'black'
  },
  categoryText: {
    fontWeight: '700',
  }
})
