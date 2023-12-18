import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bwell.search.ProviderSearchQuery
import com.bwell.sampleapp.R

class LocationAdapter(private val context: Context, private val locationList: List<ProviderSearchQuery.Location?>?) : BaseAdapter() {

    override fun getCount(): Int {
        return locationList!!.size
    }

    override fun getItem(position: Int): Any {
        return locationList?.get(position) ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: LocationViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.location_item_view, parent, false)
            holder = LocationViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as LocationViewHolder
        }

        // Bind data to views
        val locationItem = getItem(position) as ProviderSearchQuery.Location?
        holder.boldTextView.text = locationItem?.name
        holder.secondTextView.text = locationItem?.name
        val city: String? = locationItem?.address?.city
        val state: String? = locationItem?.address?.state
        val postalCode: String? = locationItem?.address?.postalCode
        holder.thirdTextView.text = city+", "+state+" "+postalCode

        return view!!
    }

    private class LocationViewHolder(itemView: View) {
        val boldTextView: TextView = itemView.findViewById(R.id.boldTextView)
        val secondTextView: TextView = itemView.findViewById(R.id.secondTextView)
        val thirdTextView: TextView = itemView.findViewById(R.id.thirdTextView)
    }
}
