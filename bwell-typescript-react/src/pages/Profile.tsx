import { useEffect, useState } from "react";
import { getUserProfile } from "@/sdk/getUserProfile";
import { updateUserProfile } from "@/sdk/updateUserProfile";

const ProfilePage = () => {
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState<any>({});
  const [saveLoading, setSaveLoading] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    getUserProfile()
      .then((profile) => {
        setProfile(profile);
        if (profile) {
          setEditData({ ...profile });
        }
      })
      .catch((err) => setError(err?.message || "Error fetching profile"))
      .finally(() => setLoading(false));
  }, []);

  const handleEdit = () => {
    setEditMode(true);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEditData({ ...editData, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setSaveLoading(true);
    setSaveError(null);
    try {
      // Directly send the flat editData
      const payload: any = { ...editData };
      // Remove empty fields
      Object.keys(payload).forEach(key => {
        if (
          payload[key] === undefined ||
          payload[key] === null ||
          (typeof payload[key] === "string" && payload[key].trim() === "")
        ) {
          delete payload[key];
        }
      });
      await updateUserProfile(payload);
      setProfile({ ...profile, ...editData });
      setEditMode(false);
    } catch (err: any) {
      setSaveError(err?.message || "Error saving profile");
    } finally {
      setSaveLoading(false);
    }
  };

  const handleCancel = () => {
    setEditMode(false);
    setSaveError(null);
  };

  if (loading) return <div>Loading profile...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!profile) return <div>No profile data found.</div>;

  // Render all fields in editData as text fields except id (readonly)
  // Only show fields that are not objects, except id, firstName, lastName
  const orderedFields = ["id", "firstName", "lastName"];
  const otherFields = Object.keys(editData)
    .filter(
      (field) =>
        !orderedFields.includes(field) &&
        (typeof editData[field] !== "object" || editData[field] === null)
    );
  const fieldsToRender = [...orderedFields.filter(f => f in editData), ...otherFields];

  return (
    <div style={{ padding: "20px", maxWidth: 600 }}>
      <h2>User Profile</h2>
      {editMode ? (
        <>
          <div style={{ marginBottom: 16 }}>
            <button type="button" onClick={handleCancel} style={{ marginRight: 10 }}>Cancel</button>
            <button type="button" onClick={handleSave} disabled={saveLoading}>
              {saveLoading ? "Saving..." : "Save"}
            </button>
          </div>
          <form onSubmit={e => { e.preventDefault(); handleSave(); }}>
            {fieldsToRender.map((field) => (
              <div key={field} style={{ marginBottom: 12 }}>
                <label style={{ display: "block", fontWeight: 500 }}>
                  {field}
                </label>
                <input
                  type="text"
                  name={field}
                  value={editData[field] || ""}
                  onChange={field === "id" ? undefined : handleChange}
                  style={{ width: "100%", padding: 8, background: field === "id" ? "#e0e0e0" : undefined, color: field === "id" ? "#888" : undefined }}
                  readOnly={field === "id"}
                  disabled={field === "id"}
                />
              </div>
            ))}
            {saveError && <div style={{ color: "red" }}>{saveError}</div>}
          </form>
        </>
      ) : (
        <>
          <button onClick={handleEdit} style={{ marginBottom: 16 }}>Edit</button>
          <pre>{JSON.stringify(profile, null, 2)}</pre>
        </>
      )}
    </div>
  );
};

export default ProfilePage;
