import { statisticsService } from '@/services/statisticsService';
import { AssistanceStatisticsResponse } from '@/types/statistics';
import {
  BarChartOutlined,
  CalendarOutlined,
  FilePdfOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Button, Card, DatePicker, Empty, Select, Space, Spin, Typography } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/fr';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { useEffect, useRef, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const { Title } = Typography;
const { RangePicker } = DatePicker;

function Statistics() {
  const [isLoading, setIsLoading] = useState(false);
  const [statistics, setStatistics] = useState<AssistanceStatisticsResponse | null>(null);
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(30, 'days'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [isPdfExporting, setIsPdfExporting] = useState(false);

  // Refs pour la capture des graphiques (éléments cachés en visibility:hidden)
  const barChartRef = useRef<HTMLDivElement>(null);
  const lineChartRef = useRef<HTMLDivElement>(null);

  // Formate une date ISO en "Samedi 28 mars 2026"
  const formatDateFull = (dateStr: string): string => {
    const formatted = dayjs(dateStr).locale('fr').format('dddd D MMMM YYYY');
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  };

  // Convertit le SVG d'un graphique en PNG data URL via canvas
  const svgToDataUrl = (container: HTMLDivElement): Promise<string | null> => {
    return new Promise((resolve) => {
      const svgEl = container.querySelector('svg');
      if (!svgEl) return resolve(null);
      const svgWidth = Number.parseFloat(svgEl.getAttribute('width') ?? '880');
      const svgHeight = Number.parseFloat(svgEl.getAttribute('height') ?? '400');
      const cloned = svgEl.cloneNode(true) as SVGSVGElement;
      cloned.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
      const svgStr = new XMLSerializer().serializeToString(cloned);
      const src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgStr);
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = svgWidth * 2;
        canvas.height = svgHeight * 2;
        const ctx = canvas.getContext('2d');
        if (!ctx) return resolve(null);
        ctx.scale(2, 2);
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, svgWidth, svgHeight);
        ctx.drawImage(img, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => resolve(null);
      img.src = src;
    });
  };

  // Exporte un rapport PDF : tableau + histogramme + courbes
  const handleExportPdf = async () => {
    if (!statistics || statistics.dailyStats.length === 0) return;
    setIsPdfExporting(true);
    try {
      const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
      const pageWidth = doc.internal.pageSize.getWidth();

      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.text("Rapport de Statistiques d'Assistance", pageWidth / 2, 18, { align: 'center' });
      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      doc.text(
        `Période : ${startDate.format('DD/MM/YYYY')} - ${endDate.format('DD/MM/YYYY')}`,
        pageWidth / 2,
        27,
        { align: 'center' }
      );

      autoTable(doc, {
        head: [['Date', 'Présentiel', 'Visio', 'Total']],
        body: statistics.dailyStats.map((stat) => [
          formatDateFull(stat.date),
          stat.inPerson,
          stat.remote,
          stat.total,
        ]),
        startY: 35,
        styles: { fontSize: 10 },
        headStyles: { fillColor: [24, 144, 255], fontStyle: 'bold' },
        columnStyles: {
          0: { cellWidth: 90 },
          1: { cellWidth: 35, halign: 'center' },
          2: { cellWidth: 35, halign: 'center' },
          3: { cellWidth: 35, halign: 'center' },
        },
      });

      if (barChartRef.current) {
        const barImg = await svgToDataUrl(barChartRef.current);
        if (barImg) {
          doc.addPage();
          doc.setFontSize(14);
          doc.setFont('helvetica', 'bold');
          doc.text('Histogramme', pageWidth / 2, 15, { align: 'center' });
          const bw = pageWidth - 20;
          const bh = (400 * bw) / 880;
          doc.addImage(barImg, 'PNG', 10, 22, bw, bh);
        }
      }

      if (lineChartRef.current) {
        const lineImg = await svgToDataUrl(lineChartRef.current);
        if (lineImg) {
          doc.addPage();
          doc.setFontSize(14);
          doc.setFont('helvetica', 'bold');
          doc.text('Courbes', pageWidth / 2, 15, { align: 'center' });
          const lw = pageWidth - 20;
          const lh = (400 * lw) / 880;
          doc.addImage(lineImg, 'PNG', 10, 22, lw, lh);
        }
      }

      doc.save(
        `rapport-assistance-${startDate.format('YYYY-MM-DD')}_${endDate.format('YYYY-MM-DD')}.pdf`
      );
    } catch (err) {
      console.error("Erreur lors de l'export PDF:", err);
    } finally {
      setIsPdfExporting(false);
    }
  };

  // Charge les statistiques
  const loadStatistics = async () => {
    setIsLoading(true);
    try {
      const data = await statisticsService.getAssistanceStatistics(
        startDate.format('YYYY-MM-DDTHH:mm:ss'),
        endDate.format('YYYY-MM-DDTHH:mm:ss')
      );
      setStatistics(data);
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error);
      setStatistics(null);
    } finally {
      setIsLoading(false);
    }
  };

  // Charge les statistiques au montage et quand les dates changent
  useEffect(() => {
    loadStatistics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startDate, endDate]);

  // Gère le changement de période
  const handleDateRangeChange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    if (dates?.[0] && dates?.[1]) {
      setStartDate(dates[0]);
      setEndDate(dates[1]);
    }
  };

  // Prépare les données pour le graphique
  const chartData =
    statistics?.dailyStats.map((stat) => ({
      date: dayjs(stat.date).format('DD/MM'),
      Présentiel: stat.inPerson,
      Visio: stat.remote,
      Total: stat.total,
    })) || [];

  // Calcule les moyennes
  const totalInPerson = statistics?.dailyStats.reduce((sum, stat) => sum + stat.inPerson, 0) || 0;
  const totalRemote = statistics?.dailyStats.reduce((sum, stat) => sum + stat.remote, 0) || 0;
  const totalAssistance = totalInPerson + totalRemote;

  // Nombre de jours avec des données
  const daysWithData = statistics?.dailyStats.length || 1;

  // Calcul des moyennes
  const avgInPerson = Math.round(totalInPerson / daysWithData);
  const avgRemote = Math.round(totalRemote / daysWithData);
  const avgTotal = Math.round(totalAssistance / daysWithData);

  // Détermine le contenu à afficher
  const renderChartContent = () => {
    if (isLoading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <p style={{ marginTop: '16px' }}>Chargement des statistiques...</p>
        </div>
      );
    }

    if (!statistics || statistics.dailyStats.length === 0) {
      return (
        <Empty
          description="Aucune donnée sauvegardée sur cette période"
          style={{ padding: '50px' }}
        />
      );
    }

    return (
      <div>
        <Title level={4}>Assistance par Jour</Title>
        <ResponsiveContainer width="100%" height={400}>
          {chartType === 'bar' ? (
            <BarChart
              data={chartData}
              margin={{
                top: 20,
                right: 30,
                left: 20,
                bottom: 5,
              }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="Présentiel" fill="#1890ff" />
              <Bar dataKey="Visio" fill="#52c41a" />
              <Bar dataKey="Total" fill="#722ed1" />
            </BarChart>
          ) : (
            <LineChart
              data={chartData}
              margin={{
                top: 20,
                right: 30,
                left: 20,
                bottom: 5,
              }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="Présentiel"
                stroke="#1890ff"
                strokeWidth={2}
                dot={{ fill: '#1890ff' }}
              />
              <Line
                type="monotone"
                dataKey="Visio"
                stroke="#52c41a"
                strokeWidth={2}
                dot={{ fill: '#52c41a' }}
              />
              <Line
                type="monotone"
                dataKey="Total"
                stroke="#722ed1"
                strokeWidth={2}
                dot={{ fill: '#722ed1' }}
              />
            </LineChart>
          )}
        </ResponsiveContainer>
      </div>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>
        <BarChartOutlined /> Statistiques d'Assistance
      </Title>

      {/* Filtres de date */}
      <Card style={{ marginBottom: '24px' }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space wrap>
            <CalendarOutlined />
            <span>Période :</span>
            <RangePicker
              value={[startDate, endDate]}
              onChange={handleDateRangeChange}
              format="DD/MM/YYYY"
              placeholder={['Date de début', 'Date de fin']}
            />
            <Select
              value={chartType}
              onChange={setChartType}
              style={{ width: 150 }}
              options={[
                { value: 'bar', label: 'Histogramme' },
                { value: 'line', label: 'Courbes' },
              ]}
            />
            <Button icon={<ReloadOutlined />} onClick={loadStatistics}>
              Actualiser
            </Button>
            {statistics && statistics.dailyStats.length > 0 && (
              <Button
                icon={<FilePdfOutlined />}
                type="primary"
                onClick={handleExportPdf}
                loading={isPdfExporting}
              >
                Exporter PDF
              </Button>
            )}
          </Space>
        </Space>
      </Card>

      {/* Statistiques globales */}
      {statistics && statistics.dailyStats.length > 0 && (
        <Card style={{ marginBottom: '24px' }}>
          <Space size="large">
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Présentiel</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
                {avgInPerson}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Visio</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#52c41a' }}>
                {avgRemote}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Totale</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{avgTotal}</div>
            </div>
          </Space>
        </Card>
      )}

      {/* Graphique */}
      <Card>{renderChartContent()}</Card>

      {/* Graphiques cachés hors écran pour la capture PDF (visibility:hidden = en DOM, invisible) */}
      {statistics && statistics.dailyStats.length > 0 && (
        <div
          aria-hidden="true"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            visibility: 'hidden',
            pointerEvents: 'none',
            zIndex: -1,
          }}
        >
          <div ref={barChartRef}>
            <BarChart
              width={880}
              height={400}
              data={chartData}
              margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="Présentiel" fill="#1890ff" isAnimationActive={false} />
              <Bar dataKey="Visio" fill="#52c41a" isAnimationActive={false} />
              <Bar dataKey="Total" fill="#722ed1" isAnimationActive={false} />
            </BarChart>
          </div>
          <div ref={lineChartRef}>
            <LineChart
              width={880}
              height={400}
              data={chartData}
              margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="Présentiel"
                stroke="#1890ff"
                strokeWidth={2}
                dot={{ fill: '#1890ff' }}
                isAnimationActive={false}
              />
              <Line
                type="monotone"
                dataKey="Visio"
                stroke="#52c41a"
                strokeWidth={2}
                dot={{ fill: '#52c41a' }}
                isAnimationActive={false}
              />
              <Line
                type="monotone"
                dataKey="Total"
                stroke="#722ed1"
                strokeWidth={2}
                dot={{ fill: '#722ed1' }}
                isAnimationActive={false}
              />
            </LineChart>
          </div>
        </div>
      )}
    </div>
  );
}

export default Statistics;
