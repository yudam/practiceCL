package com.mdy.library_process

import com.mdy.practicecl.IDataTransaction

/**
 * User: maodayu
 * Date: 2023/3/14
 * Time: 17:09
 */
object BinderProxyImpl {


    class DataTransaction : IDataTransaction.Stub() {

        override fun scheduleCreateService() {

        }

        override fun scheduleStartService() {

        }

        override fun scheduleStopService() {

        }

    }

}