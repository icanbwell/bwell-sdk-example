import { useState } from "react";
import { createTOSConsent, createDataSharingConsent, getAllConsents, getTOSConsents, testConsentFlow, getConsentsLikeCitizen } from "@/sdk/consentManager";
import Page from "./Page";

export default function ConsentTest() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<string>('');

  const handleCreateConsent = async () => {
    setLoading(true);
    setResult('Creating consent...');
    try {
      const consent = await createTOSConsent();
      setResult(`✓ Created consent: ${consent.id}\nStatus: ${consent.status}\n\nCheck console for full details.`);
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateDataSharingConsent = async () => {
    setLoading(true);
    setResult('Creating DATA_SHARING consent...');
    try {
      const consent = await createDataSharingConsent();
      setResult(`✓ Created DATA_SHARING consent: ${consent.id}\nStatus: ${consent.status}\nCategory: ${consent.category}\n\nCheck console for full details.`);
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleGetAllConsents = async () => {
    setLoading(true);
    setResult('Getting all consents...');
    try {
      const bundle = await getAllConsents();
      const count = Array.isArray(bundle) ? bundle.length : (bundle as any)?.entry?.length || 0;
      setResult(`✓ Retrieved ${count} consent(s)\n\nCheck console for full details.`);
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleGetTOSConsents = async () => {
    setLoading(true);
    setResult('Getting TOS consents...');
    try {
      const bundle = await getTOSConsents();
      const count = Array.isArray(bundle) ? bundle.length : (bundle as any)?.entry?.length || 0;
      if (count > 0) {
        setResult(`✓ Retrieved ${count} TOS consent(s)\n\nCheck console for full details.`);
      } else {
        setResult(`⚠ No TOS consents found\n\nCheck console for full details.`);
      }
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCitizenPattern = async () => {
    setLoading(true);
    setResult('Testing Citizen\'s exact pattern with beta SDK...');
    try {
      const bundle = await getConsentsLikeCitizen();
      if (bundle === null) {
        setResult(`⚠ Returned null (check console for error message)`);
      } else {
        const count = bundle.entry?.length || 0;
        setResult(
          `Beta SDK Result:\n` +
          `Bundle ID: ${bundle.id || 'N/A'}\n` +
          `Entry: ${bundle.entry ? 'present' : 'null'}\n` +
          `Consent count: ${count}\n\n` +
          `Check console for full details.`
        );
      }
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleFullTest = async () => {
    setLoading(true);
    setResult('Running full consent flow test...\n\nCheck console for detailed output.');
    try {
      const results = await testConsentFlow();
      const allCount = Array.isArray(results.allConsents) ? results.allConsents.length : 0;
      const tosCount = Array.isArray(results.tosConsents) ? results.tosConsents.length : 0;
      
      setResult(
        `✓ Test completed!\n\n` +
        `Created consent: ${results.created.id}\n` +
        `All consents: ${allCount}\n` +
        `TOS consents (filtered): ${tosCount}\n\n` +
        (tosCount > 0 
          ? '✓ SUCCESS: Filter working correctly!' 
          : '⚠ WARNING: Filter not returning TOS consent'
        ) +
        `\n\nCheck console for full details.`
      );
    } catch (error: any) {
      setResult(`✗ Error: ${error.message}\n\nCheck console for details.`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Page title="Consent Testing">
      <div style={{ padding: '20px', maxWidth: '800px' }}>
        <h2>Consent Management Test Suite</h2>
        <p>Test consent creation and retrieval to debug Citizen's issue.</p>

        <div style={{ 
          display: 'flex', 
          flexDirection: 'column', 
          gap: '10px', 
          marginTop: '20px',
          marginBottom: '20px'
        }}>
          <button 
            onClick={handleFullTest} 
            disabled={loading}
            style={{ 
              padding: '15px', 
              fontSize: '16px', 
              fontWeight: 'bold',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Running...' : '▶ Run Full Test (Create + Retrieve)'}
          </button>

          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: '1fr 1fr', 
            gap: '10px',
            marginTop: '10px'
          }}>
            <button 
              onClick={handleCreateConsent} 
              disabled={loading}
              style={{ 
                padding: '15px', 
                cursor: loading ? 'not-allowed' : 'pointer',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                fontWeight: 'bold',
                fontSize: '15px'
              }}
            >
              ✓ Create TOS Consent (Citizen's Pattern)
            </button>

            <button 
              onClick={handleCitizenPattern} 
              disabled={loading}
              style={{ 
                padding: '15px', 
                cursor: loading ? 'not-allowed' : 'pointer',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                fontWeight: 'bold',
                fontSize: '15px'
              }}
            >
              ✓ Get TOS Consents (Citizen's Pattern)
            </button>
          </div>

          <div style={{ 
            marginTop: '10px', 
            padding: '10px', 
            backgroundColor: '#d4edda', 
            borderRadius: '5px',
            fontSize: '13px',
            border: '1px solid #c3e6cb'
          }}>
            <strong>✓ Green buttons above</strong> use Citizen's exact code patterns with beta SDK 2.0.0-beta-rc.1764971887
          </div>

          <details style={{ marginTop: '10px' }}>
            <summary style={{ cursor: 'pointer', fontWeight: 'bold', padding: '10px', backgroundColor: '#e9ecef', borderRadius: '5px' }}>
              📋 Additional Test Buttons (Different Patterns)
            </summary>
            <div style={{ 
              display: 'grid', 
              gridTemplateColumns: '1fr 1fr 1fr', 
              gap: '10px',
              marginTop: '10px'
            }}>
              <button 
                onClick={handleGetAllConsents} 
                disabled={loading}
                style={{ 
                  padding: '10px', 
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                Get All Consents
              </button>
              
              <button 
                onClick={handleGetTOSConsents} 
                disabled={loading}
                style={{ 
                  padding: '10px', 
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                Get TOS Consents (Filtered)
              </button>

              <button 
                onClick={handleCreateDataSharingConsent} 
                disabled={loading}
                style={{ 
                  padding: '10px', 
                  cursor: loading ? 'not-allowed' : 'pointer',
                  backgroundColor: '#6f42c1',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                }}
              >
                Create DATA_SHARING
              </button>
            </div>
          </details>
        </div>

        <div style={{ 
          padding: '15px', 
          backgroundColor: '#f5f5f5', 
          borderRadius: '5px',
          minHeight: '150px',
          whiteSpace: 'pre-wrap',
          fontFamily: 'monospace',
          fontSize: '14px'
        }}>
          {result || 'Click a button to run tests. Check browser console for detailed logs.'}
        </div>

        <div style={{ 
          marginTop: '20px', 
          padding: '15px', 
          backgroundColor: '#fff3cd', 
          borderRadius: '5px',
          fontSize: '14px'
        }}>
          <strong>💡 Citizen's Code Patterns:</strong>
          <ul>
            <li><strong>Create:</strong> Uses <code>.failure()</code> check, returns <code>consent.data() ?? null</code></li>
            <li><strong>Get:</strong> Uses <code>consents.error</code> property, returns <code>consents.data ?? null</code></li>
            <li><strong>Category:</strong> Uppercase <code>'TOS'</code> for beta SDK 2.0.0-beta-rc.1764971887</li>
            <li><strong>Known Issue:</strong> Beta SDK retrieve returns <code>entry: null</code> (backend bug)</li>
            <li><strong>Workaround:</strong> Skip getConsents() - just create consents without checking</li>
          </ul>
        </div>
      </div>
    </Page>
  );
}
