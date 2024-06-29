import { Box, Container } from "@mui/material"

interface Resource {
    content: string;
}

interface HealthData {
    data: {
        resources?: Resource[];
    };
}

interface DisplayKnowledgeProps {
    name: string;
    healthData: HealthData;
}

export const DisplayKnowledge: React.FC<DisplayKnowledgeProps> = ({ name, healthData }) => {
    return <Container>
        {healthData?.data?.resources?.length &&
            <>
                <h2>{name} Knowledge: HTML</h2>
                <div dangerouslySetInnerHTML={{ __html: healthData.data.resources?.map(r => r.content).join('') }} />
            </>
        }
        <h2>{name} Knowledge: JSON</h2>
        <Box>
            <pre style={{ textWrap: 'wrap' }}>{JSON.stringify(healthData, null, 2)}</pre>
        </Box>
    </Container>
}