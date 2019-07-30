package cn.com.start.cloudprinter.startcloudprinter.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation

import cn.com.start.cloudprinter.startcloudprinter.R
import kotlinx.android.synthetic.main.devs_fragment.*
import kotlinx.android.synthetic.main.devs_fragment.fab
import kotlinx.android.synthetic.main.fragment_config.*

class DevsFragment : Fragment() {

    companion object {
        fun newInstance() = DevsFragment()
    }

    private lateinit var viewModel: DevsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.devs_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DevsViewModel::class.java)
        // TODO: Use the ViewModel

        fab.setOnClickListener { v ->
            fab.setOnClickListener { v ->
                Navigation.findNavController(v).navigate(R.id.action_devsFragment2_to_devFragment2)
            }
        }
    }

}
