package bon.jo.jtots.core

import java.io.InputStream
import java.util
import java.util.zip.{ZipEntry, ZipFile}

import scala.collection.mutable.ArrayBuffer

object ZipOp {
  def iterator(zipInputStream: ZipFile): Iterable[(String, Array[Byte])] = {
    new Iterable[(String, Array[Byte])] {
      override def iterator: Iterator[(String, Array[Byte])] = {
        val optionIter: Iterator[Option[(String, Array[Byte])]] = new Iterator[Option[(String, Array[Byte])]] {
          val ent: util.Enumeration[_ <: ZipEntry] = zipInputStream.entries()

          override def hasNext: Boolean = ent.hasMoreElements

          override def next(): Option[(String, Array[Byte])] = {
            Option[ZipEntry](ent.nextElement()).flatMap(e => {
              if (e.getName.endsWith(".class")) {
                Option(e)
              } else {
                None
              }
            }).map((entry: ZipEntry) => {
              (entry.getName, {
                val zipIn: InputStream = zipInputStream.getInputStream(entry)
                val buff = ArrayBuffer[Byte]()
                val ret = new Array[Byte](512)

                var ecr = zipIn.read(ret, 0, 512)
                if (ecr != -1) {
                  buff ++= ret.slice(0, ecr)
                }
                while (ecr != -1) {
                  ecr = zipIn.read(ret, 0, 512)
                  if (ecr != -1) {
                    buff ++= ret.slice(0, ecr)
                  }
                }
                buff.toArray
              })
            })
          }

        }
        optionIter.filter(_.isDefined).map(_.get)
      }
    }
  }

  implicit class ZipOpOn(zipInputStream: ZipFile) {
    def iterator(): Iterable[(String, Array[Byte])] = {
      ZipOp.iterator(zipInputStream)
    }
  }

}
