import withAuthCheck from "@/components/withAuthCheck";

const HealthData = () => {
    return (<h1>Health Data</h1>);
}

export default withAuthCheck('Health Data', HealthData);