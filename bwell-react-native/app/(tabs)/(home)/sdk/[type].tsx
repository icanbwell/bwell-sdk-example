import { useBWellSDK } from '@/components/sdk'
import { AllergyIntolerancesGroupsResults, AllergyIntolerancesResults } from '@icanbwell/bwell-sdk-ts'
import { useQuery } from '@tanstack/react-query'
import { useLocalSearchParams, useNavigation } from 'expo-router'
import { useEffect } from 'react'
import { View, Text, StyleSheet, FlatList } from 'react-native'

type AllergyIntoleranceItem = AllergyIntolerancesGroupsResults['resources'][number]

export default function Category() {
  const nav = useNavigation()
  const { type } = useLocalSearchParams()

  useEffect(() => {
    nav.setOptions({
      title: type
    })
  }, [nav, type])

  switch (type) {
    case 'ALLERGY_INTOLERANCE':
      return <AllergyIntolerances />
  }

  return (
    <View><Text>WUT YOU DOIN, {type}?</Text></View>
  )
}

export function Header({ type }: { type: string }) {
  const { sdk } = useBWellSDK()

  const healthSummary = useQuery({
    queryKey: ['health-summary'],
    queryFn: () => {
      return sdk.health.getHealthSummary()
    }
  })

  const resources = healthSummary.data?.data?.resources ?? [];
  const category = resources.find((resource) => {
    return resource.category === type
  })

  if (category === undefined) {
    return <Text>{type}</Text>
  }

  return (
    <View style={styles.headerContainer}>
      <Text style={styles.headerText}>Total: {category.total ?? 0}</Text>
    </View>
  )
}

function AllergyIntolerances() {
  const { sdk } = useBWellSDK()
  const allergies = useQuery({
    queryKey: ['allergy-intolerances'],
    queryFn: () => {
      return sdk.health.getAllergyIntoleranceGroups();
    }
  })

  if (allergies.isLoading) {
    return (
      <View>
        <Text>Loading Allergy Intolerances</Text>
      </View>
    )
  }

  const allergyData = allergies.data?.data?.resources ?? [];

  return (
    <View style={styles.container}>
      <Header type="ALLERGY_INTOLERANCE" />
      <FlatList
        data={allergyData}
        renderItem={(item) => {
          return (
            <AllergyIntoleranceListItem allergy={item.item} />
          )
        }}
        keyExtractor={(item, index) => item.id ?? index.toString()}
      />
    </View>
  )
}

function AllergyIntoleranceListItem({ allergy }: { allergy: AllergyIntoleranceItem }) {
  const text = allergy.name ?? 'n/a';
  const criticality = allergy.criticality?.display ?? allergy.criticality?.code ?? 'n/a';

  return (
    <View style={styles.allergyIntoleranceItem}>
      <Text style={styles.allergyName}>{text}</Text>
      <Text>crit: {criticality}</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  headerContainer: {
    padding: 8,
  },

  headerText: {
    fontWeight: 800,
    fontSize: 24
  },

  allergyIntoleranceItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 8,
    borderBottomColor: 'black'
  },

  allergyName: {
    fontWeight: 600,
  }
})
