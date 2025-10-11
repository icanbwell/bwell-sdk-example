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
      .then((result) => {
        const data = result?.data;
        setProfile(data);
        if (data) {
          // Flatten nested fields to flat format
          const flat: any = { ...data };
          if (data.address && Array.isArray(data.address) && data.address[0]) {
            flat.addressStreet = data.address[0].line?.[0] || "";
            flat.city = data.address[0].city || "";
            flat.stateOrProvidence = data.address[0].state || "";
            flat.postageOrZipCode = data.address[0].postalCode || "";
          }
          if (data.telecom && Array.isArray(data.telecom)) {
            flat.homePhone = data.telecom.find((t: any) => t.system === "phone" && t.use === "home")?.value || "";
            flat.mobilePhone = data.telecom.find((t: any) => t.system === "phone" && t.use === "mobile")?.value || "";
            flat.workPhone = data.telecom.find((t: any) => t.system === "phone" && t.use === "work")?.value || "";
            flat.email = data.telecom.find((t: any) => t.system === "email")?.value || "";
          }
          if (data.name && Array.isArray(data.name) && data.name[0]) {
            flat.firstName = data.name[0].given?.[0] || "";
            flat.lastName = data.name[0].family || "";
          }
          // Remove parent fields that are objects/arrays and have children already mapped
          ["address", "telecom", "name"].forEach((field) => {
            if (flat[field]) {
              delete flat[field];
            }
          });
          // Fix: If any remaining field is an object, convert to string or pick a sensible value
          Object.keys(flat).forEach((key) => {
            if (typeof flat[key] === "object" && flat[key] !== null) {
              if (Array.isArray(flat[key])) {
                flat[key] = flat[key].join(", ");
              } else {
                flat[key] = JSON.stringify(flat[key]);
              }
            }
          });
          setEditData(flat);
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
