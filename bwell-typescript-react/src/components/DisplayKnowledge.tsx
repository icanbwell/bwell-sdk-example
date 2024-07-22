import { RootState } from "@/store/store";
import { selectToggle, toggleValue } from "@/store/toggleSlice";
import { Box, Container, ToggleButton, ToggleButtonGroup } from "@mui/material"
import { useDispatch, useSelector } from "react-redux";

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
    const TOGGLE_LOCATOR = `${name}-knowledge`;

    const showHtml = useSelector((state: RootState) => selectToggle(state, TOGGLE_LOCATOR)) && healthData?.data?.resources?.length;

    const dispatch = useDispatch();

    const handleToggle = () => {
        dispatch(toggleValue(TOGGLE_LOCATOR));
    };

    return <Container>
        <h2>{name} Knowledge</h2>
        <Box sx={{ padding: '5px' }}>
            <ToggleButtonGroup
                value={showHtml ? 'html' : 'json'}
                exclusive
                onChange={handleToggle}
            >
                <ToggleButton value="html">HTML</ToggleButton>
                <ToggleButton value="json">JSON</ToggleButton>
            </ToggleButtonGroup>
        </Box>
        {showHtml && healthData.data.resources && (
          <div dangerouslySetInnerHTML={{ __html: healthData.data.resources.map(r => r.content).join('') }} />
        )}
        {!showHtml && <pre style={{ textWrap: 'wrap' }}>{JSON.stringify(healthData, null, 2)}</pre>}
    </Container>
}